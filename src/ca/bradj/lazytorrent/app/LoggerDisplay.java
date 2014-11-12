package ca.bradj.lazytorrent.app;

import java.util.Arrays;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import ca.bradj.Layouts;
import ca.bradj.fx.FXThreading;

public class LoggerDisplay {

    protected static final int MAX_NUM_LINES_LOW = 50;
    protected static final int MAX_NUM_LINES_DEBUG = 300;
    private final TextArea text;
    private final VBox node;
    private final TextArea low;
    private final TextField countdown;

    public LoggerDisplay(ObservableValue<? extends String> countdownProperty) {
        this.text = new TextArea();
        text.setWrapText(true);
        text.setMinHeight(400);
        text.setMaxHeight(Double.MAX_VALUE);
        text.setPrefHeight(400);
        text.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                final String[] split = newValue.split("\n");
                if (split.length < MAX_NUM_LINES_DEBUG) {
                    return;
                }
                FXThreading.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        text.setText(lastNLines(split, MAX_NUM_LINES_DEBUG));
                        text.setScrollTop(Double.MAX_VALUE);
                    }
                });
            }
        });

        node = Layouts.vbox();
        low = new TextArea();
        low.setPrefHeight(200);
        low.setMinHeight(200);
        low.setPrefRowCount(20);
        low.setWrapText(true);
        low.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                final String[] split = newValue.split("\n");
                if (split.length < MAX_NUM_LINES_LOW) {
                    return;
                }
                FXThreading.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        low.setText(lastNLines(split, MAX_NUM_LINES_LOW));
                        low.setScrollTop(Double.MAX_VALUE);
                    }
                });
            }
        });

        countdown = new TextField();
        countdown.textProperty().bind(countdownProperty);

        node.getChildren().addAll(low, text, countdown);
    }

    protected String lastNLines(String[] split, int maxNumLinesLow) {
        String[] copyOfRange = Arrays.copyOfRange(split, split.length - maxNumLinesLow + 1, split.length);
        StringBuilder sb = new StringBuilder();
        for (String i : copyOfRange) {
            sb.append(i + "\n");
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    public Node getNode() {
        return node;
    }

}
