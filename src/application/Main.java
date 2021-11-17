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
/*
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
*/
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxListCell;
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
    public static final ObservableList<String> weathers = 
        FXCollections.observableArrayList();
    public static final ObservableList<String> pops = 
        FXCollections.observableArrayList();

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

        HBox views = new HBox();
        final ListView<String> listView_weathers = new ListView<>(weathers);
        final ListView<String> listView_pops = new ListView<>(pops);
        listView_weathers.setPrefSize(200, 250);
        listView_weathers.setEditable(true);
        listView_pops.setPrefSize(200, 250);
        listView_pops.setEditable(true);
        weathers.addAll(" ");
        pops.add(" ");
        listView_weathers.setItems(weathers);
        listView_pops.setItems(pops);
        views.getChildren().addAll(listView_weathers, listView_pops);


                        
            
        root.getChildren().add(views);

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

                String[] Name_array = new String[kuiki_result_Name.size()]; 
                System.out.println("++++++++++++++");
                for (int i = 0; i < kuiki_result_Name.size(); i++) {
                    System.out.println("--------------");
                    String tmp_arr[] = kuiki_result_Name.get(i).split("\n");
                    Name_array[i] = tmp_arr[0];
                    
                }
                

                //weathers
                for (int i = 0; i < weathers.size(); i++) {
                    System.out.println(weathers.get(i));
                    
                }

                weathers.clear();
            
                
                for (int i = 0; i < Name_array.length; i++){
                    weathers.add(Name_array[i]);
                    for (int j = 0; j < weather_array[i].length; j++) {
                        weathers.add(weather_array[i][j]);
                    }
                }
                

                //pops
                for (int i = 0; i < pops.size(); i++) {
                    System.out.println(pops.get(i));
                    
                }

                
                pops.clear();
                    
                
                for (int i = 0; i < Name_array.length; i++){
                    pops.add(Name_array[i]);
                    pops.add(" ");
                    for (int j = 0; j < POP_array[i].length -1 ; j++) {
                        pops.add(POP_array[i][j]);
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