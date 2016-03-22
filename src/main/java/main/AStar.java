package main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by b.istomin on 22.03.2016.
 */
public class AStar {
    public static int width = 0;
    public static int height = 0;

    /*
     * Размер сетки и координаты препятствий считываются из файла. Координаты начала и конца - с клавиатуры.
     */
    public static void main(String[] args) {

        //read data from file
        Scanner sysIn = new Scanner(System.in);

        System.out.println("enter absolute file name for grid size and blockage info");
        String fileName = sysIn.next();

        Map<String, String> map = new HashMap<String, String>();
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(fileName));
        } catch (FileNotFoundException e) {
            System.out.println("file not found!");
            e.printStackTrace();
        }

        String line = "";

        //get grid size
        try {
            line = in.readLine();
            String parts[] = line.split(" ");
            width = Integer.parseInt(parts[0]);
            height = Integer.parseInt(parts[1]);
        } catch (IOException e) {
            System.out.println("error while getting grid size");
            e.printStackTrace();
        }

        // Создадим все нужные списки
        Table<Cell> cellList = new Table<Cell>(AStar.width, AStar.height);
        Table blockList = new Table(AStar.width, AStar.height);
        LinkedList<Cell> openList = new LinkedList<Cell>();
        LinkedList<Cell> closedList = new LinkedList<Cell>();
        LinkedList<Cell> tmpList = new LinkedList<Cell>();

        try {
            while ((line = in.readLine()) != null) {
                String parts[] = line.split(" ");
                blockList.add(new Cell(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), true));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Заполним карту как-то клетками, учитывая преграду
        for (int i = 0; i < AStar.width; i++) {
            for (int j = 0; j < AStar.height; j++) {
                cellList.add(new Cell(j, i, blockList.get(j, i).blocked));
            }
        }
        //start and end points coords from keyboard
        System.out.println("enter X coordinate of start");
        String keyboardString = sysIn.next();
        int xStart = Integer.parseInt(keyboardString);
        System.out.println("enter Y coordinate of start");
        keyboardString = sysIn.next();
        int yStart = Integer.parseInt(keyboardString);
        System.out.println("enter X coordinate of finish");
        keyboardString = sysIn.next();
        int xFinish = Integer.parseInt(keyboardString);
        System.out.println("enter Y coordinate of finish");
        keyboardString = sysIn.next();
        int yFinish = Integer.parseInt(keyboardString);

        // Стартовая и конечная
        cellList.get(xStart, yStart).setAsStart();
        cellList.get(xFinish, yFinish).setAsFinish();
        Cell start = cellList.get(xStart, yStart);
        Cell finish = cellList.get(xFinish, yFinish);

        //cellList.printp();

        boolean found = false;
        boolean noroute = false;

        //1) Добавляем стартовую клетку в открытый список.
        openList.push(start);

        //2) Повторяем следующее:
        while (!found && !noroute) {
            //a) Ищем в открытом списке клетку с наименьшей стоимостью F. Делаем ее текущей клеткой.
            Cell min = openList.getFirst();
            for (Cell cell : openList) {
                // тут я специально тестировал, при < или <= выбираются разные пути,
                // но суммарная стоимость G у них совершенно одинакова. Забавно, но так и должно быть.
                if (cell.F < min.F) min = cell;
            }

            //b) Помещаем ее в закрытый список. (И удаляем с открытого)
            closedList.push(min);
            openList.remove(min);
            //System.out.println(openList);

            //c) Для каждой из соседних 8-ми клеток ...
            tmpList.clear();
            tmpList.add(cellList.get(min.x - 1, min.y - 1));
            tmpList.add(cellList.get(min.x, min.y - 1));
            tmpList.add(cellList.get(min.x + 1, min.y - 1));
            tmpList.add(cellList.get(min.x + 1, min.y));
            tmpList.add(cellList.get(min.x + 1, min.y + 1));
            tmpList.add(cellList.get(min.x, min.y + 1));
            tmpList.add(cellList.get(min.x - 1, min.y + 1));
            tmpList.add(cellList.get(min.x - 1, min.y));

            for (Cell neightbour : tmpList) {
                //Если клетка непроходимая или она находится в закрытом списке, игнорируем ее. В противном случае делаем следующее.
                if (neightbour.blocked || closedList.contains(neightbour)) continue;

                //Если клетка еще не в открытом списке, то добавляем ее туда. Делаем текущую клетку родительской для это клетки. Расчитываем стоимости F, G и H клетки.
                if (!openList.contains(neightbour)) {
                    openList.add(neightbour);
                    neightbour.parent = min;
                    neightbour.H = neightbour.mandist(finish);
                    neightbour.G = start.price(min);
                    neightbour.F = neightbour.H + neightbour.G;
                    continue;
                }

                // Если клетка уже в открытом списке, то проверяем, не дешевле ли будет путь через эту клетку. Для сравнения используем стоимость G.
                if (neightbour.G + neightbour.price(min) < min.G) {
                    // Более низкая стоимость G указывает на то, что путь будет дешевле. Эсли это так, то меняем родителя клетки на текущую клетку и пересчитываем для нее стоимости G и F.
                    neightbour.parent = min; // вот тут я честно хз, надо ли min.parent или нет
                    neightbour.H = neightbour.mandist(finish);
                    neightbour.G = start.price(min);
                    neightbour.F = neightbour.H + neightbour.G;
                }

                // Если вы сортируете открытый список по стоимости F, то вам надо отсортировать свесь список в соответствии с изменениями.
            }

            //d) Останавливаемся если:
            //Добавили целевую клетку в открытый список, в этом случае путь найден.
            //Или открытый список пуст и мы не дошли до целевой клетки. В этом случае путь отсутствует.

            if (openList.contains(finish)) {
                found = true;
            }

            if (openList.isEmpty()) {
                noroute = true;
            }
        }

        //3) Сохраняем путь. Двигаясь назад от целевой точки, проходя от каждой точки к ее родителю до тех пор, пока не дойдем до стартовой точки. Это и будет наш путь.
        if (!noroute) {
            Cell rd = finish.parent;
            while (!rd.equals(start)) {
                rd.road = true;
                rd = rd.parent;
                if (rd == null) break;
            }
            //cellList.printp();
            cellList.printRouteCoords();
        } else {
            System.out.println("NO ROUTE");
        }

    }
}
