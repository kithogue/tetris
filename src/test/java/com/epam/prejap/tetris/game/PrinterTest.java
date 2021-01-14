package com.epam.prejap.tetris.game;

import com.epam.prejap.tetris.block.Color;
import com.epam.prejap.tetris.data.HallOfFame;
import com.epam.prejap.tetris.data.HallOfFameMember;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.InputMismatchException;

import static org.testng.Assert.*;

@Test(groups = "Timer")
public class PrinterTest {

    private ByteArrayOutputStream bos;
    private final byte[][] emptyGrid = new byte[][]{new byte[]{}};

    @BeforeMethod
    public void setUp() {
        bos = new ByteArrayOutputStream();
    }

    @DataProvider
    public Object[][] validTimerDurations() {
        return new Object[][]{{500, 14, "Time: 00:00:07"},
                {1000, 167, "Time: 00:02:47"},
                {1000, 4224, "Time: 01:10:24"},
                {3600000, 25, "Time: 25:00:00"},
                {1000, 25, "Time: 00:00:25"},
                {1000, 84, "Time: 00:01:24"},
                {1000, 5840, "Time: 01:37:20"},
                {3600000, 26, "Time: 26:00:00"}};
    }

    @Test(dataProvider = "validTimerDurations")
    public void drawShouldPrintValidHeader(int tickDurationInMillis, int cycles, String message) {
        // given
        Timer timer = new Timer(tickDurationInMillis);
        Printer printer = Mockito.spy(new Printer(new PrintStream(bos), timer));
        for (int i = 0; i < cycles; i++) {
            timer.tick();
        }
        // when
        printer.draw(emptyGrid);
        // then
        Mockito.verify(printer).header();
        assertTrue(bos.toString().contains(message));
    }


    @Test(groups = "Color", dataProvider = "colors")
    public void checkIfPrintMethodPrintsStringWithAppropriateColor(Color color) {
        // given
        Timer timer = Mockito.mock(Timer.class);
        Printer printer = Mockito.spy(new Printer(new PrintStream(bos), timer));
        int ansiCode = color.getAnsiCode();
        String escape =  "\u001B[";
        String finalByte = "m";
        String resetColor = escape + "0" + finalByte;
        String blockMark = "#";
        String expected = escape + ansiCode + finalByte + blockMark + resetColor;

        // when
        printer.print(color.getId());

        // then
        assertEquals(bos.toString(), expected);
    }


    @DataProvider
    private Object[] colors() {
        return Color.values();
    }

    @Test(dataProvider = "mockHallOfFame30Members")
    public void hallOfFameShallPrintMax25Entries(HallOfFameMember[] mockMembers) {
        //given
        SoftAssert softAssert = new SoftAssert();
        Timer timer = new Timer(1);
        Printer printer = new Printer(new PrintStream(bos), timer);
        HallOfFame hallOfFame = new HallOfFame(null, null, null, Arrays.asList(mockMembers));

        //when
        printer.hallOfFame(hallOfFame);

        //then
        softAssert.assertTrue(bos.toString().contains("1"));
        softAssert.assertTrue(bos.toString().contains("25"));
        softAssert.assertFalse(bos.toString().contains("26"));
        softAssert.assertFalse(bos.toString().contains("30"));
        softAssert.assertAll("Shall print only 25 elements, but it did not");

    }

    @Test(dataProvider = "playerInitialsLongerThan3Chars")
    public void shallReducePlayersNameWhenLongerThan3Chars(String name) {
        //given
        SoftAssert softAssert = new SoftAssert();
        Timer timer = new Timer(1);
        Printer printer = new Printer(new PrintStream(bos), timer);

        //when
        HallOfFameMember actual = printer.readInitials(1, name);

        //then
        softAssert.assertEquals(actual.name().length(), 3);
        softAssert.assertEquals(actual.name(), name.substring(0, 3));
        softAssert.assertAll("Shall shorten name to 3 characters but it did not");
    }

    @Test(dataProvider = "playerInitialsNoLongerThan3Chars")
    public void shallNotReducePlayersNameWhenNoLongerThan3Chars(String name) {
        //given
        Timer timer = new Timer(1);
        Printer printer = new Printer(new PrintStream(bos), timer);

        //when
        HallOfFameMember actual = printer.readInitials(1, name);

        //then
        assertEquals(actual.name(), name);
    }

    @DataProvider()
    public static Object[] mockHallOfFame30Members() {
        HallOfFameMember[] mock1 = new HallOfFameMember[30];
        Arrays.fill(mock1, new HallOfFameMember("one", 1));
        return new Object[]{mock1};
    }

    @DataProvider()
    public static Object[] playerInitialsLongerThan3Chars() {
        return new Object[]{
                "oneone",
                "twotwo",
                "123123",
                "......",
                "!`!!`!"
        };
    }

    @DataProvider()
    public static Object[] playerInitialsNoLongerThan3Chars() {
        return new Object[]{
                "one",
                "12",
                "1",
                "",
        };
    }
}
