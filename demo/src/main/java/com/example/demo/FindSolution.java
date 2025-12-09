package com.example.demo;

import java.util.HashSet;
import java.util.Set;

public class FindSolution {
//    Input: (1)data; (2)target
//    data: a list/array of numbers
//    target: a number
//    Output: [x, y], x and y are two elements in the data array that satisfy x-y == target

    public static int[] findSolutions(int[] data, int target){
        Set<Integer> seen = new HashSet<>();
        for (int x : data){
            int y1 = x- target;
            int y2 = x + target;
            if (seen.contains(y1)){
                return new int[]{x, y1};
            }
            if (seen.contains(y2)) {
                return new int[]{x, y2};
            }
            seen.add(x);
        }
        return null;
    }


    public static void main(String[] args){
        int[] data = {4, 2, 1};
        int target = 3;

        int[] result = findSolutions(data, target);
        if(result != null){
            System.out.println("find number: [" + result[0] + ", " + result[1] + "]");
        } else {
            System.out.println("not found any pair number");
        }
    }
}
