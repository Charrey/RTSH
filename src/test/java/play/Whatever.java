package play;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashSet;

public class Whatever {

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        FileInputStream fileIn = new FileInputStream("object.dat");
        ObjectInputStream objectIn = new ObjectInputStream(fileIn);
        HashSet<Integer> obj = (HashSet<Integer>) objectIn.readObject();
        System.out.println(obj);
    }
}
