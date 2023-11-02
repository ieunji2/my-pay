package com.hello.money;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class LearningTest {

  @Test
  @DisplayName("배열에 값 추가하기")
  void appendElementToArray() {
    //given
    String[] strArray = {"AA", "BB", "CC"};
    final String ee = "EE";

    //when
    strArray = appendElement(strArray, ee);

    //then
    System.out.println("newArray: " + Arrays.toString(strArray));
    assertThat(strArray.length).isEqualTo(4);
    assertThat(strArray[3]).isEqualTo(ee);
  }

  private <T> T[] appendElement(T[] originArray, T element) {
    final T[] newArray = Arrays.copyOf(originArray, originArray.length + 1);
    newArray[originArray.length] = element;
    return newArray;
  }
}
