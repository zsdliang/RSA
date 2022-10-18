import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Main {

    public static void main(String[] args) throws IOException {
        Random random = new Random();
        int p_length = 1024;
        int q_length = 1024;

        BigInteger p = BigInteger.probablePrime(p_length,random);
        BigInteger q = BigInteger.probablePrime(q_length,random);

        BigInteger n = p.multiply(q);

        BigInteger fn = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));

        BigInteger e = getE(p_length,q_length,fn,random);

        BigInteger d = getD(e,fn);

        Encrypt(e,n);
        Decrypt(d,n);
    }

    /**
     *计算e
     */
    private static BigInteger getE(int p_length, int q_length, BigInteger fn, Random random) {
        BigInteger e = BigInteger.probablePrime(p_length,random);
        while(!(e.gcd(fn).equals(BigInteger.ONE))) {
            if(e.compareTo(fn) > 0) {
                e = BigInteger.probablePrime(p_length,random);
            }
            e.add(BigInteger.TEN);
        }
        return e;
    }

    /**
     *计算d
     */
    public static BigInteger getD(BigInteger e,BigInteger fn) {
        return e.modInverse(fn);
    }

    /**
     *加密
     */
    public static void Encrypt(BigInteger e, BigInteger n) throws IOException {
        Path path = Path.of("file/lab2_cFile.txt");
        File mFile = new File("file/lab2-Plaintext.txt");
        File cFile = new File("file/lab2_cFile.txt");

        FileInputStream inputStream = new FileInputStream(mFile);
        FileOutputStream outputStream = new FileOutputStream(cFile);

        byte [] bytes = inputStream.readAllBytes();
        List<Integer> mCodes = ConvertToNum(bytes);
        List<BigInteger> bigBats = PackBat(mCodes);

        List<BigInteger> cCodes = new ArrayList<>();

        for(BigInteger mCode : bigBats) {
            cCodes.add(mCode.modPow(e,n));
        }
        long length = log2(n);
        List<String> cCodeStrings = toStringList(cCodes);
        List<String> stringList_fmt = FormatString(cCodeStrings,length);

        for(String str: stringList_fmt) {
            outputStream.write(str.getBytes(StandardCharsets.UTF_8));
        }
        inputStream.close();
        outputStream.close();
    }

    /**
     *解密
     */
    public static void Decrypt(BigInteger d,BigInteger n) throws IOException {
        Path path = Path.of("file/lab2_cFile.txt");
        File cFile = new File("file/lab2_cFile.txt");
        File mFile = new File("file/lab2_mFile.txt");
        FileInputStream inputStream = new FileInputStream(cFile);
        FileOutputStream outputStream = new FileOutputStream(mFile);

        long batLength = log2(n);

        String cCode = Files.readString(path);
        List<String> cCodes = Arrays.asList(cCode.split("(?<=\\G.{" + batLength + "})"));

        List<BigInteger> cDatas = toBigNumList(cCodes);
        List<BigInteger> mBats = new ArrayList<>();

        for(BigInteger item:cDatas) {
            mBats.add(item.modPow(d,n));
        }

        List<Character> characters = ConvertToChars(SplitBat(mBats));

        for(Character character:characters) {
            outputStream.write(character);
        }

        inputStream.close();
        outputStream.close();
    }


    /**
     *将字母数字转成数字10-71
     */
    private static List<Integer> ConvertToNum(byte [] bytes) {
        int i;
        int bytes_length = bytes.length;
        List<Integer> mCodes = new ArrayList<>();

        for(i = 0;i < bytes_length;i++) {
            if((char)bytes[i]<='9' && (char)bytes[i]>='0') {
                mCodes.add(bytes[i]-38); //0-9映射到10-19
            }
            else if((char)bytes[i]<='z' && (char)bytes[i]>='a') {
                mCodes.add(bytes[i]-77); //a-z映射到20-45
            }
            else if((char)bytes[i]<='Z' && (char)bytes[i]>='A') {
                mCodes.add(bytes[i]-19); //A-Z映射到46-71
            }
            else {
                continue;
            }
        }
        return  mCodes;
    }

    /**
     *将明文码拆分（4位变两个2位）
     */
    private static List<Integer> SplitBat(List<BigInteger> bats) {
        int c0,c1;
        int temp;
        List<Integer> mCodes = new ArrayList<>();
        for(BigInteger bat:bats) {
            temp = bat.intValue();
            c1 = temp % 100;
            c0 = (temp - c1)/100;
            mCodes.add(c0);
            mCodes.add(c1);
        }
        return mCodes;
    }

    /**
     *将明文码转成明文
     */
    private static List<Character> ConvertToChars(List<Integer> mCodes) {
        List<Character> characters = new ArrayList<>();
        if(mCodes.get(mCodes.size()-1) == 0) {
            mCodes.remove(mCodes.size()-1);
        }
        for(Integer integer:mCodes) {
            if(integer <= 19 && integer >= 10) {
                characters.add((char)(integer + 38)); //10-19映射到0-9
            }
            else if(integer <= 45 && integer >= 20) {
                characters.add((char)(integer + 77)); //20-45映射到a-z
            }
            else if(integer <= 71 && integer >= 46) {
                characters.add((char)(integer + 19)); //46-71映射到A-Z
            }
        }
        return characters;
    }

    /**
     *将明文码组成两个一组
     */
    private static List<BigInteger> PackBat(List<Integer> mCodes) {
        int c0;
        int c1;
        int bat;
        int length = mCodes.size();
        boolean isOdd = (length % 2 == 1)?true:false;
        int i;
        List<BigInteger> mCodeBat = new ArrayList<>();

        if(isOdd) {
            for(i = 0;i < length-1;i= i+2) {
                c0 = mCodes.get(i);
                c1 = mCodes.get(i+1);
                bat = c0*100 + c1;
                mCodeBat.add(new BigInteger(String.valueOf(bat)));
            }
            mCodeBat.add(new BigInteger(String.valueOf(mCodes.get(i)*100)));
        } else {
            for(i = 0;i < length;i= i+2) {
                c0 = mCodes.get(i);
                c1 = mCodes.get(i+1);
                bat = c0*100 + c1;
                mCodeBat.add(new BigInteger(String.valueOf(bat)));
            }
        }
        return mCodeBat;
    }

    /**
     *密文前端补零
     */
    private static List<String> FormatString(List<String> originString,long length) {
        List<String> strings_fmt = new ArrayList<>();
        for(String string:originString) {
            StringBuilder sb = new StringBuilder();
            while (sb.length() < length - string.length()) {
                sb.append('0');
            }
            sb.append(string);
            strings_fmt.add(sb.toString());
        }
        return strings_fmt;
    }

    /**
     *大数转字符串
     */
    private static List<String> toStringList(List<BigInteger> BigList) {
        List<String> stringList = new ArrayList<>();
        for(BigInteger big : BigList) {
            stringList.add(big.toString());
        }
        return stringList;
    }


    /**
     *字符串转大数
     */
    private static List<BigInteger> toBigNumList(List<String> cCodes) {
        List<BigInteger> bigIntList = new ArrayList<>();
        for(String cCode:cCodes) {
            bigIntList.add(new BigInteger(cCode));
        }
        return bigIntList;
    }

    /**
     *用于确定密文一组的长度
     */
    private static long log2(BigInteger n) {
        long i = 1l;
        BigInteger temp = BigInteger.ONE;
        while(temp.compareTo(n) < 0) {
            temp = temp.multiply(BigInteger.TWO);
            i++;
        }
        return i/2;
    }

}