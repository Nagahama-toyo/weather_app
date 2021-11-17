package application;
import java.util.ArrayList;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Main extends Application {
    TableView<Result> table;
    ObservableList<Result> data;

    @Override
    public void start(final Stage stage) {
        VBox root = new VBox();
        HBox btns = new HBox();
        Button startBtn = new Button("検索開始");
        Button stopBtn = new Button("stop");
        TextField input_prefecture = new TextField("滋賀県");
        Label label = new Label("都道府県名を入力してください(例)；滋賀県");

        root.setAlignment(Pos.CENTER);
        btns.getChildren().addAll(startBtn, stopBtn);
        root.getChildren().addAll(label, input_prefecture, btns);

        stage.setTitle("天気予報したるで");

        Scene scene = new Scene(root, 300, 100);

        EventHandler<ActionEvent> startBtnActionFilter = (event) -> {
            String regular_search_query;
            System.out.println("start button was pushed!");

            
            String kuiki_search_Weather;
            String kuiki_search_POP;
            String kuiki_search_Name;
            String chiten_search;


            ArrayList<String> kuiki_result_Weather = new ArrayList<>();
            //ProbabilityOfPrecipitation(POP)
            ArrayList<String> kuiki_result_POP = new ArrayList<>();
            ArrayList<String> kuiki_result_Name = new ArrayList<>();
            //ArrayList<String> chiten_result = new ArrayList<>();

            if (input_prefecture.getText() != null && !input_prefecture.getText().isEmpty()) {
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");
                String fileName = "regular_" + now.format(dateTimeFormatter);
                String link = "http://www.data.jma.go.jp/developer/xml/feed/regular.xml";

                File out = new File(
                        "C:/Users/toitt/Documents/B3_SecondSemester/DesignProject_Java/weather_19t2021j/regular_xml/"
                                + fileName + ".xml");
                Thread first = new Thread(new application.XMLDownloader(link, out));
                first.start();

                try {
                    first.join();
                } catch (Exception e) {
                    System.out.println("Failed to Download");
                }
                System.out.println("XMLDownload join finished");
                regular_search_query = "//entry[content='【" + input_prefecture.getText() + "府県週間天気予報】']/id";
                System.out.println(regular_search_query);
                RegularXMLThread analyzer = new application.RegularXMLThread(out, regular_search_query);
                analyzer.start();
                try {
                    analyzer.join();
                } catch (Exception e) {
                    System.out.println("analyzer join failed");
                }
                File tmp_xml = analyzer.getResult();
                System.out.println("analyzer join finished");

                kuiki_search_Weather = "//MeteorologicalInfos[@type='区域予報']/TimeSeriesInfo/Item/Kind/Property/WeatherPart";
                kuiki_search_POP = "//MeteorologicalInfos[@type='区域予報']/TimeSeriesInfo/Item/Kind/Property/ProbabilityOfPrecipitationPart";
                kuiki_search_Name = "//MeteorologicalInfos[@type='区域予報']/TimeSeriesInfo/Item/Area/Name";
                chiten_search = "//MeteorologicalInfos[@type='地点予報']/TimeSeriesInfo/Item/Kind/Property/TemperaturePart";

                PreXMLAnalyzer kuiki_weather = new PreXMLAnalyzer(tmp_xml, kuiki_search_Weather);
                PreXMLAnalyzer kuiki_POP = new PreXMLAnalyzer(tmp_xml, kuiki_search_POP);
                PreXMLAnalyzer kuiki_Name = new PreXMLAnalyzer(tmp_xml, kuiki_search_Name);
                PreXMLAnalyzer chiten = new PreXMLAnalyzer(tmp_xml, chiten_search);

                kuiki_weather.start();
                kuiki_POP.start();
                kuiki_Name.start();
                chiten.start();

                try {
                    kuiki_weather.join();
                    kuiki_POP.join();
                    kuiki_Name.join();
                    chiten.join();
                } catch (Exception e) {
                    System.out.println("tmp_xml Threads join failed");
                }

                kuiki_result_Weather = kuiki_weather.getResult();
                kuiki_result_POP = kuiki_POP.getResult();
                kuiki_result_Name = kuiki_Name.getResult();
                //chiten_result = chiten.getResult();

                String[][] weather_array = new String[kuiki_result_Weather.size()][7]; 
                System.out.println("++++++++++++++");
                for (int i = 0; i < kuiki_result_Weather.size(); i++) {
                    System.out.println("--------------");
                    String tmp_arr[] = kuiki_result_Weather.get(i).split("\n");
                    for (int h = 0; h < tmp_arr.length; h++){
                        weather_array[i][h] = tmp_arr[h];
                        System.out.println(weather_array[i][h]);
                    }
                    
                }
                
                String[][] POP_array = new String[kuiki_result_POP.size()][7]; 
                System.out.println("++++++++++++++");
                for (int i = 0; i < kuiki_result_POP.size(); i++) {
                    System.out.println("--------------");
                    String tmp_arr[] = kuiki_result_POP.get(i).split("\n");
                    for (int h = 0; h < tmp_arr.length; h++){
                        POP_array[i][h] = tmp_arr[h];
                        System.out.println(POP_array[i][h]);
                    }
                    
                }

                String[][] Name_array = new String[kuiki_result_Name.size()][7]; 
                System.out.println("++++++++++++++");
                for (int i = 0; i < kuiki_result_Name.size(); i++) {
                    System.out.println("--------------");
                    String tmp_arr[] = kuiki_result_Name.get(i).split("\n");
                    for (int h = 0; h < tmp_arr.length; h++){
                        Name_array[i][h] = tmp_arr[h];
                        System.out.println(Name_array[i][h]);
                    }
                    
                }

                label.setText("都道府県名を入力してください(例)；滋賀県");
            } else {
                label.setText("入力されていません");
            }

            event.consume();
        };
        startBtn.addEventHandler(ActionEvent.ANY, startBtnActionFilter);

        EventHandler<ActionEvent> stopBtnActionFilter = (event) -> {
            System.out.println("stop button was pushed!");
            table = new TableView<>();
            table.setEditable(true);
            
            
            
            data = FXCollections.observableArrayList();
            table.itemsProperty().setValue(data);
            table.setItems(data);
            
            TableColumn<Result, String> weatherCol = new TableColumn<>("天気");
            TableColumn<Result, String> POPCol = new TableColumn<>("降水確率");
            table.getColumns().addAll(weatherCol,POPCol);

            HBox hbox = new HBox();
            hbox.setSpacing(5);
            hbox.setPadding(new Insets(10, 0, 0, 10));
            hbox.getChildren().addAll(label, table);
            root.getChildren().addAll(hbox);

            data.addAll( new Result("aaa ", " bbb") );
            table.itemsProperty().setValue(data);
            table.setItems(data);

            event.consume();
        };
        stopBtn.addEventHandler(ActionEvent.ANY, stopBtnActionFilter);

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}