import java.io.*;
import java.util.*;

public class EightPuzzle {
    // Global Variables
    private static String state = "b12 345 678";
    private static final String goalState = "b12 345 678";
    private static int row = 0;
    private static int col = 0;
    private static int MAXNODES = 1000;
    private static long millis = System.currentTimeMillis() % 1000;
    private static String h;    // heuristic choice for each search algorithm
    private static final String[] startValue = {"0", null, null};
    private static final String[] directions = {"left", "right", "up", "down"};
    // maps each unique state node to its path cost, parent node, and the direction that led to the state from the initial state.
    private static HashMap<String, String[]> map = new HashMap<String, String[]>();
    // The priority queue prioritizes by the lowest f(n) value of a state.
    private static PriorityQueue<String> queue =
                    new PriorityQueue<String>(
                    (a, b) -> EightPuzzle.getF(a, Integer.parseInt(map.get(a)[0]), h) -
                    EightPuzzle.getF(b, Integer.parseInt(map.get(b)[0]), h));

    /*
     * Depending on the command line input entered in by the user,
     * the function will call that command's method.
     * @params: the command line input.
     */
    private static void command(String cmd) {
        String[] cmds = cmd.split(" ");
        int length = cmds.length;
        String input = cmds[length - 1];

        switch(cmds[0]) {
            case "setState":
                EightPuzzle.setState(cmd.substring(9, cmd.length()));
                System.out.println("================STATE SET");
                break;
            case "randomizeState":
                EightPuzzle.randomizeState(Integer.parseInt(input));
                break;
            case "printState":
                EightPuzzle.printState();
                break;
            case "move":
                int result = EightPuzzle.move(input);
                if (result < 0)
                    System.out.println("================ERROR: INVALID MOVE");
                else
                    System.out.println("================VALID MOVE");
                break;
            case "solve":
                if (cmds[1].equals("A-star")) {
                    System.out.println("================STARTING SEARCH");
                    EightPuzzle.solveAStar(input);
                }
                else if (cmds[1].equals("beam")) {
                    System.out.println("================STARTING SEARCH");
                    EightPuzzle.solveBeam(Integer.parseInt(input));
                }
                else
                    System.out.println("================ERROR: INVALID SEARCH");
                break;
            case "maxNodes":
                EightPuzzle.maxNodes(Integer.parseInt(input));
                break;
            default:
                System.out.println("An invalid command was inputted!");
                break;
        }
    }

    /*
     * Sets the current state to the user's specified state
     * @params: the new state in the form of "b## ### ###".
     */
    private static void setState(String state) {
        EightPuzzle.state = state;

        String tempState = state.replaceAll("\\s+", "");    // flatten the state.
        int tempRow = 0;
        int tempCol = 0;
        // find the row and col index for the blank tile from the new state.
        int i = 0;
        while (tempState.charAt(i) != 'b') {
            tempCol++;
            if (tempCol == 3) {
                tempRow++;
                tempCol = 0;
            }
            i++;
        }

        row = tempRow;
        col = tempCol;
    }

    /*
     * Makes n random moves from the current state.
     * @params: number of desired random moves.
     */
    private static void randomizeState(int n) {
        Random rand = new Random();
        // loop n times
        for (int i = 0; i < n; i++) {
            // generate a random number
            int r = rand.nextInt(100) + 1;
            // depending on the randon number, move up, down, left, or right.
            int result = 0;
            if (r <= 25)
                result = EightPuzzle.move("left");
            else if (r > 25 && r <= 50)
                result = EightPuzzle.move("down");
            else if (r > 50 && r <= 75)
                result = EightPuzzle.move("right");
            else if (r > 75 && r <= 100)
                result = EightPuzzle.move("up");
            else
                System.out.println("Something went wrong!");

            if (result < 0)
                i--;
        }
        System.out.printf("%d randomized move(s) have been done!\n", n);
    }

    /*
     * Prints out the current state in "b## ### ###" form and
     * a 3x3 board of the current state.
     */
    private static void printState() {
        System.out.printf("================STATE: %s\n", state);

        // Build and print out board.
        StringBuilder board = new StringBuilder();
        for (int i = 0; i < state.length(); i++) {
            board.append("|");
            char tile = state.charAt(i);
            if (tile != ' ') {
                if (tile != 'b')
                    board.append(tile);
                else
                    board.append(" ");
            } else
                board.append("\n");

            if (i == state.length() - 1)
                board.append("|");
        }

        System.out.println(board.toString());
    }

    /*
     * Moves the blank tile in the current state in the desired direction.
     * @params: a String representing the direction ("left", "right", etc.).
     * @returns: 0 if the move was possible, or 1 if the move was not possible.
     */
    private static int move(String direction) {
        String[] board = state.split(" ");
        int replaceRow = row;
        int replaceCol = col;
        char replaceChar;

        // shift the blank tile one space in the specified direction.
        switch(direction) {
            case "up":
                if (row > 0) {
                    replaceRow -= 1;
                    replaceChar = board[replaceRow].charAt(replaceCol);
                    board[replaceRow] = board[replaceRow].replace(replaceChar, 'b');
                    board[row] = board[row].replace('b', replaceChar);
                    row -= 1;
                } else {
                    return -1;
                }
                break;
            case "down":
                if (row < 2) {
                    replaceRow += 1;
                    replaceChar = board[replaceRow].charAt(replaceCol);
                    board[replaceRow] = board[replaceRow].replace(replaceChar, 'b');
                    board[row] = board[row].replace('b', replaceChar);
                    row += 1;
                } else {
                    return -1;
                }
                break;
            case "left":
                if (col > 0) {
                    replaceCol -= 1;
                    replaceChar = board[replaceRow].charAt(replaceCol);
                    board[row] = board[row].replace(replaceChar, 'r');
                    board[row] = board[row].replace('b', replaceChar);
                    board[row] = board[row].replace('r', 'b');
                    col -= 1;
                } else {
                    return -1;
                }
                break;
            case "right":
                if (col < 2) {
                    replaceCol += 1;
                    replaceChar = board[replaceRow].charAt(replaceCol);
                    board[row] = board[row].replace(replaceChar, 'r');
                    board[row] = board[row].replace('b', replaceChar);
                    board[row] = board[row].replace('r', 'b');
                    col += 1;
                } else {
                    return -1;
                }
                break;
            default:
                System.out.println("Invalid direction for move command!");
                return -1;
        }

        state = String.join(" ", board[0], board[1], board[2]);
        return 0;
    }

    /*
     * Run an A-star search algorithm to solve the puzzle.
     * @params: a String determining the heuristic to use for the search ("h1" or "h2").
     * @sys.out: the amount of time the search took, the shortest path cost, and the moves needed to get to the goal state.
     */
    private static void solveAStar(String heuristics) {
        h = heuristics;
        String nodeState = state;
        String startState = state;
        map.put(nodeState, startValue);
        int pathCost = 1;
        // millis = System.currentTimeMillis() % 1000;
        millis = System.nanoTime()/1000000;
        // loop until the next state to search is the goal state.
        while (queue.size() <= MAXNODES && !nodeState.equals(goalState)) {
            String tempState;
            int result;
            // for each direction add the state to the queue.
            for (String dir : directions) {
                result = EightPuzzle.move(dir);
                if (result == 0) { // checks if the direction of the move is possible.
                    tempState = state;
                    String[] values = {"" + pathCost + "", nodeState, dir};
                    if (map.get(tempState) == null) { // checks if the state has not been reached yet.
                        map.put(tempState, values);
                        queue.offer(tempState);
                    } else {
                        // if the path cost for this state is less than the previous path cost,
                        // replace the old path cost and update the queue.
                        int oldCost = Integer.parseInt(map.get(tempState)[0]);
                        if (pathCost < oldCost) {
                            map.put(tempState, values);
                            queue.remove(tempState);
                            queue.offer(tempState);
                        }
                    }
                }
                // reset the state to the node state that is currently being expanded.
                EightPuzzle.setState(nodeState);
            }
            // set the next priority state and remove it from the queue
            nodeState = queue.remove();
            EightPuzzle.setState(nodeState);
            pathCost = Integer.parseInt(map.get(nodeState)[0]) + 1;
        }
        long endTime = System.nanoTime()/1000000;
        millis = Math.abs(endTime - millis);
        // millis = Math.abs(System.currentTimeMillis() % 1000 - millis);
        // check if the number of nodes exceeded the max number of nodes allowed.
        if (queue.size() > MAXNODES) {
            System.out.println("ERROR! Max number of nodes limit exceeded!");
            System.out.printf("MAXNODES = %d; Number of Nodes used = %d\n", MAXNODES, queue.size());
            System.out.println("================FAIL");
            EightPuzzle.setState(startState);
        } else {
            System.out.printf("The search took %d milliseconds.\n", millis);
            System.out.printf("MAXNODES = %d; Number of Nodes used = %d\n", MAXNODES, queue.size());
            // print out the lowest path cost to the goal state.
            System.out.printf("Total Number of Tile Moves = %d\n", pathCost - 1);
            // print out the moves to get to the goal state.
            String path = "goal";
            while (map.get(nodeState)[1] != null) {
                path = map.get(nodeState)[2] + " -> " + path;
                nodeState = map.get(nodeState)[1];
            }
            path = "start -> " + path;
            System.out.printf("SOLUTION PATH:\n%s\n", path);
            System.out.println("================PASS");
        }

        queue.clear();
        map.clear();
    }

    /*
     * Calculate the sum of the path cost and the heuristic value of the state; f(n) = g(n) + h(n).
     * @params: a String of the state that needs calculations for, the path cost to this state (g), and the heuristic for the search (h).
     * @returns: the total cost of the inputted state.
     */
    private static int getF(String state, int pathCost, String heuristic) {
        int h;
        if (heuristic.equals("h1"))
            h = EightPuzzle.getH1(state);
        else
            h = EightPuzzle.getH2(state);
        return h + pathCost;
    }

    /*
     * Calculate the heuristic of how many tiles are in an incorrect position.
     * @params: a String of the state that needs calculations for.
     * @returns: the number of tiles in an incorrect position.
     */
    private static int getH1(String state) {
        int h1 = 0;
        String tempState = state.replaceAll("\\s+", ""); // flatten string
        // loop through the flattened state string and count how many tiles are in the wrong position.
        for (int i = 0; i < 9; i++) {
            char tile = tempState.charAt(i);
            if (tile != 'b') {
                if (i != Character.getNumericValue(tile))
                    h1++;
            } else {
                if (i != 0)
                    h1++;
            }
        }
        return h1;
    }

    /*
     * Calculate the heuristic of the total number of moves away every tile is from their goal position.
     * @params: a String of the state that needs calculations for.
     * @returns: the total nunmber of moves away every tile is from their goal position.
     */
    private static int getH2(String state) {
        int h2 = 0;
        String tempState = state.replaceAll("\\s+", ""); // flatten string
        // loop through the flattened state string and sum up the path cost for a tile to be in its correct position.
        for (int i = 0; i < 9; i++) {
            char tile = tempState.charAt(i);
            int diff = 0;
            // absolute difference between target index and actual index
            diff = (tile != 'b') ? Math.abs(i - Character.getNumericValue(tile)) : i - 0;
            h2 += diff/3 + diff%3; // number of rows from target + number of cols from target
        }
        return h2;
    }

    /*
     * Run a local beam search algorithm to solve the puzzle.
     * @params: the number of states the priority queue may hold.
     * @sys.out: the amount of time the search took, the shortest path cost, and the moves needed to get to the goal state.
     */
    private static void solveBeam(int k) {
        h = "h2";
        String nodeState = state;
        String startState = state;
        map.put(nodeState, startValue);
        int pathCost = 1;
        String lowPState = state;   // lowest priority state
        // millis = System.currentTimeMillis() % 1000;
        millis = System.nanoTime()/1000000;
        while (!nodeState.equals(goalState)) {
            String tempState;
            int result;
            // for each direction add the state to the queue.
            for (String dir : directions) {
                result = EightPuzzle.move(dir);
                if (queue.size() < k) {
                    if (result == 0) { // checks if the direction of the move is possible.
                        tempState = state;
                        // checks if the current state is lowest prioritized state
                        // and if true: replace the current lowest prioritized state.
                        if (EightPuzzle.getF(tempState, pathCost, "h2") >
                            EightPuzzle.getF(lowPState, Integer.parseInt(map.get(lowPState)[0]), "h2"))
                            lowPState = tempState;
                        // checks if the current lowest prioritized state is the start state
                        // and if true: replace with current state;
                        if (lowPState.equals(startState))
                            lowPState = tempState;
                        String[] values = {"" + pathCost + "", nodeState, dir};
                        if (map.get(tempState) == null) {   // checks if the state has not been reached yet.
                            map.put(tempState, values);
                            queue.offer(tempState);
                        } else {
                            // if the path cost for this state is less than the previous path cost,
                            // replace the old path cost and update the queue.
                            int oldCost = Integer.parseInt(map.get(tempState)[0]);
                            if (pathCost < oldCost) {
                                map.put(tempState, values);
                                queue.remove(tempState);
                                queue.offer(tempState);
                            }
                        }
                    }
                } else {
                    if (result == 0) {
                        tempState = state;
                        String[] values = {"" + pathCost + "", nodeState, dir};
                        if (map.get(tempState) == null) {   // checks if the state has not been reached yet.
                            if (EightPuzzle.getF(tempState, pathCost, "h2") >
                                EightPuzzle.getF(lowPState, Integer.parseInt(map.get(lowPState)[0]), "h2")) {
                                map.put(tempState, values);
                                queue.remove(lowPState);
                                queue.offer(tempState);
                            }
                        } else {
                            if (EightPuzzle.getF(tempState, pathCost, "h2") >
                                EightPuzzle.getF(lowPState, Integer.parseInt(map.get(lowPState)[0]), "h2")) {
                                // if the path cost for this state is less than the previous path cost,
                                // replace the old path cost and update the queue.
                                int oldCost = Integer.parseInt(map.get(tempState)[0]);
                                if (pathCost < oldCost) {
                                    map.put(tempState, values);
                                    queue.remove(tempState);
                                    queue.offer(tempState);
                                }
                            }
                        }
                    }
                }
                // reset the state to the node state that is currently being expanded.
                EightPuzzle.setState(nodeState);
            }
            // set the next priority state and remove it from the queue
            nodeState = queue.remove();
            EightPuzzle.setState(nodeState);
            pathCost = Integer.parseInt(map.get(nodeState)[0]) + 1;
        }
        long endTime = System.nanoTime() / 1000000;
        millis = Math.abs(endTime - millis);
        System.out.printf("The search took %d milliseconds.\n", millis);
        // print out the lowest path cost to the goal state.
        System.out.printf("Total Number of Tile Moves = %d\n", pathCost - 1);
        // print out the moves to get to the goal state.
        String path = "goal";
        while (map.get(nodeState)[1] != null) {
            path = map.get(nodeState)[2] + " -> " + path;
            nodeState = map.get(nodeState)[1];
        }
        path = "start -> " + path;
        System.out.printf("SOLUTION PATH:\n%s\n", path);
        System.out.println("================PASS");

        queue.clear();
        map.clear();
    }

    /*
     * Sets the max number of nodes A-star search may hold in its priority queue.
     * @params: an integer representing the max number of nodes.
     */
    private static void maxNodes(int n) {
        MAXNODES = n;
        System.out.printf("The max number of nodes was set to %d!\n", n);
    }

    /*
     * May accept a text file containing a list of commands or
     * command line input from the user.
     * @params: a text file as a string(optional).
     */
    public static void main(String[] args) {
        int length = args.length;
        if (length > 0) {
            String file = args[0];
            int fileLen = file.length();
            if (file.substring(fileLen - 3, fileLen).equals("txt")) {
                try {
                    BufferedReader input = new BufferedReader(new FileReader(args[0]));
                    String line;
                    while ((line = input.readLine()) != null) {
                        System.out.println(line);
                        EightPuzzle.command(line);
                    }
                } catch (IOException e) {
                    System.err.println(e);
                    System.out.println("ERROR: Problem reading file.");
                }
            } else {
                System.out.println("ERROR: text file not inputted.");
            }
        } else {
            Scanner in = new Scanner(System.in);
            boolean loop = true;
            System.out.println("Enter a command!");
            // Continue to accept user inputs until asked to "quit".
            while (loop) {
                String cmd = in.nextLine();
                if (cmd.equals("quit")) {
                    loop = false;
                } else {
                    EightPuzzle.command(cmd);
                }
            }
        }
    }
}
