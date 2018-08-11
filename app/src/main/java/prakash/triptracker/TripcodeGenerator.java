package prakash.triptracker;

import java.util.Random;

class TripcodeGenerator {

    private static final String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final String digits = "0123456789";

    private static final String alphanum = upper + digits;

    public static String generateTripcode(int length){

        //Create a random
        Random random = new Random();
        //Convert the alphanum string to char array
        char[] chars = alphanum.toCharArray();

        //Buffer array to store our random value
        char[] buf = new char[length];

        for(int i=0;i<length;i++){
            buf[i] = chars[random.nextInt(chars.length)];
        }

        return new String(buf);
    }
}
