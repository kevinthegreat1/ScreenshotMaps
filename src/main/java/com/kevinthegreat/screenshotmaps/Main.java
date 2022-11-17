package com.kevinthegreat.screenshotmaps;

import com.google.common.io.Files;
import com.kevinthegreat.screenshotmaps.util.Grid;
import com.kevinthegreat.screenshotmaps.util.Point;
import com.kevinthegreat.screenshotmaps.util.Triple;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.*;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

public class Main {
    private static final WebDriver driver;
    private static String runDir;

    static {
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        driver = new ChromeDriver(options);
    }

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in); BufferedReader reader = new BufferedReader(new FileReader(runDir = scanner.nextLine()))) {
            runDir = runDir.substring(0, runDir.lastIndexOf("/"));
            int zoom = parseZoom(reader);
            CompletableFuture<Void> saveScreenshot = CompletableFuture.completedFuture(null);
            for (Point point : new Grid(parseLine(reader, 2), parseLine(reader, 3))) {
                driver.get("https://map.baidu.com/@" + point + "," + zoom + "z");
                removeElements();
                saveScreenshot = CompletableFuture.allOf(saveScreenshot, takeScreenshot(point, zoom));
            }
            driver.quit();
            saveScreenshot.join();
        } catch (FileNotFoundException e) {
            driver.quit();
            throw new RuntimeException("File not found at the specified path", e);
        } catch (IOException e) {
            driver.quit();
            throw new RuntimeException("An error occurred while cleaning up", e);
        }
    }

    public static int parseZoom(BufferedReader reader) {
        try {
            return Integer.parseInt(reader.readLine());
        } catch (IOException e) {
            driver.quit();
            throw new RuntimeException("Error while reading file, try running this again", e);
        } catch (NumberFormatException e) {
            driver.quit();
            throw new RuntimeException("Line 1 (zoom level) is not a number", e);
        }
    }

    public static Triple<Integer, Integer, Integer> parseLine(BufferedReader reader, int lineNumber) {
        try {
            String line = reader.readLine();
            if (line == null) {
                driver.quit();
                throw new IllegalArgumentException("File missing line " + lineNumber);
            }
            String[] info = line.split(" ");
            if (info.length != 3) {
                driver.quit();
                throw new IllegalArgumentException("File missing arguments or has too many arguments at line " + lineNumber);
            }
            return new Triple<>(Integer.parseInt(info[0]), Integer.parseInt(info[1]), Integer.parseInt(info[2]));
        } catch (IOException e) {
            driver.quit();
            throw new RuntimeException("Error while reading file, try running this again", e);
        } catch (NumberFormatException e) {
            driver.quit();
            throw new IllegalArgumentException("File contains invalid number at line " + lineNumber, e);
        }
    }

    public static void removeElements() {
        ((JavascriptExecutor) driver).executeScript("""
                const closeLogin = document.getElementById("TANGRAM__PSP_36__closeBtn");
                if (closeLogin != null) {
                    closeLogin.click();
                }
                document.getElementById("left-panel").remove();
                document.getElementById("tooltip-route").remove();
                document.getElementById("app-right-top").remove();
                document.getElementById("map-operate").remove();
                document.getElementById("mapType-wrapper").remove();
                document.getElementById("newuilogo").remove();
                document.getElementById("map-bottom-tip").remove();
                document.getElementsByClassName(" BMap_scaleCtrl anchorBR")[0].remove();
                document.getElementsByClassName(" BMap_cpyCtrl BMap_noprint anchorBL")[0].remove()""");
    }

    private static CompletableFuture<Void> takeScreenshot(Point point, int zoom) {
        driver.manage().window().fullscreen();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException("Error while waiting for map to load, try running this again", e);
        }
        File tempFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        return CompletableFuture.runAsync(() -> {
            try {
                File file = new File(runDir + "/" + point + "," + zoom + "z.png");
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
                Files.copy(tempFile, file);
            } catch (IOException e) {
                throw new RuntimeException("Error while saving screenshot at" + point + "," + zoom + "z, try running this again", e);
            }
        });
    }
}