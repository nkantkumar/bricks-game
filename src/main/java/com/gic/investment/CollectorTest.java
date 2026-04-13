package com.gic.investment;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class CollectorTest {
    public static void main(String[] args) {

        Stream<String> tokens = Pattern.compile("\\s+")
                .splitAsStream("Hello World Java");
        List s= Arrays.stream("Hello World Java".split("\\s+")).toList();
        System.out.println(s);

        Stream<BigInteger> fibonacci = Stream.iterate(
                new BigInteger[] {BigInteger.ZERO, BigInteger.ONE},
                arr -> new BigInteger[] {arr[1], arr[0].add(arr[1])}
        ).map(arr -> arr[0]);
        String[] a = new String[]{"1"};
    }
}
