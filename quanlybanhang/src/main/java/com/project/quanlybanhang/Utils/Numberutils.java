package com.project.quanlybanhang.Utils;

public class Numberutils {

    public static String parseSafeLongadddot(String number){


        String parsenumber = Long.toString(Long.parseLong(number));

        int dem=0;
        String showstart = "";

        for(int i = parsenumber.length()-1 ; i >= 0;i--){
            if(dem%3 == 0){
                showstart += '.';
                showstart += parsenumber.charAt(i);
                dem++;
            }else{
                showstart += parsenumber.charAt(i);
                dem++;
            }
        }

        String finish = "";
        for(int i = showstart.length()-1 ; i >= 0;i--){
            finish += showstart.charAt(i);

        }
        return finish;
    }
}
