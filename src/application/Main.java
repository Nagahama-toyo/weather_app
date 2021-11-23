package application;
import java.util.ArrayList;

import javafx.collections.*;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main extends Application {
    public static final ObservableList<String> weathers = 
        FXCollections.observableArrayList();
    public static final ObservableList<String> pops = 
        FXCollections.observableArrayList();
    public static Label alert = new Label(" ");

    @Override
    public void start(final Stage stage) {
        VBox root = new VBox();
        HBox btns = new HBox();
        Button searchBtn = new Button("検索開始");
        Button stopBtn = new Button("stop");
        TextField input_prefecture = new TextField("滋賀県");
        Label label = new Label("都道府県名を入力してください(例)；滋賀県");
        //static final Label alert = new Label(" ");

        root.setAlignment(Pos.CENTER);
        btns.getChildren().addAll(searchBtn, stopBtn);
        root.getChildren().addAll(label, input_prefecture, alert, btns);

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

        EventHandler<ActionEvent> searchBtnActionFilter = (event) -> {
            String regular_search_query;
            //System.out.println("start button was pushed!");

            
            String kuiki_search_Weather;
            String kuiki_search_POP;
            String kuiki_search_Name;


            ArrayList<String> kuiki_result_Weather = new ArrayList<>();
            //ProbabilityOfPrecipitation(POP)
            ArrayList<String> kuiki_result_POP = new ArrayList<>();
            ArrayList<String> kuiki_result_Name = new ArrayList<>();

            if (input_prefecture.getText() != null && !input_prefecture.getText().isEmpty()) {
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");
                String fileName = "regular_" + now.format(dateTimeFormatter);
                String link = "http://www.data.jma.go.jp/developer/xml/feed/regular.xml";

                File out = new File(
                        "C:/Users/toitt/Documents/B3_SecondSemester/DesignProject_Java/weather_app/regular_xml/"
                                + fileName + ".xml");
                Thread first_donwload = new Thread(new application.XMLDownloader(link, out));
                first_donwload.start();

                try {
                    first_donwload.join();
                } catch (Exception e) {
                    System.out.println("Failed to Download");

                }
                
                //検索対象が北海道か否かで処理が大きく違う
                String name_prefecture = input_prefecture.getText();
                if(name_prefecture.equals("北海道")){
                    regular_search_query = "//entry[content='【" + name_prefecture + "地方週間天気予報】']/id";
                    RegularXMLThread analyzer = new application.RegularXMLThread(out, regular_search_query);
                    analyzer.start();
                    try {
                        analyzer.join();
                    } catch (Exception e) {
                        System.out.println("analyzer join failed");
                        alert.setText("regular.xml解析に失敗しました");
                    }
                    
                    File tmp_xml = analyzer.getResult();
                    //System.out.println("analyzer join finished");
                    
                    String hokkaido_search = "//Body/Comment/Text";
                    PreXMLAnalyzer hokkaido_weather = new PreXMLAnalyzer(tmp_xml, hokkaido_search);

                    hokkaido_weather.start();

                    try {
                        hokkaido_weather.join();
                    } catch (Exception e) {
                        System.out.println("tmp_xml Threads join failed");
                        alert.setText("XML解析に失敗しました");
                    }

                    ArrayList<String> hokkaido_result = new ArrayList<>();
                    hokkaido_result = hokkaido_weather.getResult();
                    Label hokkaido = new Label(hokkaido_result.get(0));
                    root.getChildren().clear();
                    root.getChildren().addAll(label, input_prefecture, alert, btns);
                    root.getChildren().addAll(hokkaido);


                } else {
                    root.getChildren().clear();
                    root.getChildren().addAll(label, input_prefecture, alert, btns);
                    root.getChildren().add(views);

                    regular_search_query = "//entry[content='【" + name_prefecture + "府県週間天気予報】']/id";
                    RegularXMLThread analyzer = new application.RegularXMLThread(out, regular_search_query);
                    analyzer.start();
                    try {
                        analyzer.join();
                    } catch (Exception e) {
                        System.out.println("analyzer join failed");
                        alert.setText("regular.xml解析に失敗しました");
                    }
                    
                    File tmp_xml = analyzer.getResult();
                    //System.out.println("analyzer join finished");
                    
                    kuiki_search_Weather = "//MeteorologicalInfos[@type='区域予報']/TimeSeriesInfo/Item/Kind/Property/WeatherPart";
                    kuiki_search_POP = "//MeteorologicalInfos[@type='区域予報']/TimeSeriesInfo/Item/Kind/Property/ProbabilityOfPrecipitationPart";
                    kuiki_search_Name = "//MeteorologicalInfos[@type='区域予報']/TimeSeriesInfo/Item/Area/Name";

                    PreXMLAnalyzer kuiki_weather = new PreXMLAnalyzer(tmp_xml, kuiki_search_Weather);
                    PreXMLAnalyzer kuiki_POP = new PreXMLAnalyzer(tmp_xml, kuiki_search_POP);
                    PreXMLAnalyzer kuiki_Name = new PreXMLAnalyzer(tmp_xml, kuiki_search_Name);

                    kuiki_weather.start();
                    kuiki_POP.start();
                    kuiki_Name.start();

                    try {
                        kuiki_weather.join();
                        kuiki_POP.join();
                        kuiki_Name.join();
                    } catch (Exception e) {
                        System.out.println("tmp_xml Threads join failed");
                        alert.setText("XML解析に失敗しました");
                    }

                    kuiki_result_Weather = kuiki_weather.getResult();
                    kuiki_result_POP = kuiki_POP.getResult();
                    kuiki_result_Name = kuiki_Name.getResult();

                    String[][] weather_array = new String[kuiki_result_Weather.size()][7]; 
                    for (int i = 0; i < kuiki_result_Weather.size(); i++) {
                        String tmp_arr[] = kuiki_result_Weather.get(i).split("\n");
                        for (int h = 0; h < tmp_arr.length; h++){
                            weather_array[i][h] = tmp_arr[h];
                        }
                        
                    }
                    
                    String[][] POP_array = new String[kuiki_result_POP.size()][7]; 
                    for (int i = 0; i < kuiki_result_POP.size(); i++) {
                        String tmp_arr[] = kuiki_result_POP.get(i).split("\n");
                        for (int h = 0; h < tmp_arr.length; h++){
                            POP_array[i][h] = tmp_arr[h];
                        }
                        
                    }

                    String[] Name_array = new String[kuiki_result_Name.size()]; 
                    for (int i = 0; i < kuiki_result_Name.size(); i++) {
                        String tmp_arr[] = kuiki_result_Name.get(i).split("\n");
                        Name_array[i] = tmp_arr[0];
                        
                    }
                    

                    //weathers
                    weathers.clear();
                    for (int i = 0; i < Name_array.length; i++){
                        weathers.add(Name_array[i]);
                        for (int j = 0; j < weather_array[i].length; j++) {
                            weathers.add(weather_array[i][j]);
                        }
                    }
                    

                    //pops
                    pops.clear();
                    for (int i = 0; i < Name_array.length; i++){
                        pops.add(Name_array[i]);
                        pops.add(" ");
                        for (int j = 0; j < POP_array[i].length -1 ; j++) {
                            pops.add(POP_array[i][j]);
                        }
                    }

                    //views
                    DateTimeFormatter day_format = DateTimeFormatter.ofPattern("dd");
                    Integer today =  Integer.parseInt(now.format(day_format));
                    views.getChildren().clear();
                    for (int i = 0; i < Name_array.length; i++) {
                        ObservableList<String> tmp_wList = FXCollections.observableArrayList();
                        for (int j = 0; j < 8; j++) {

                            if(j == 0){
                                tmp_wList.add(weathers.get(j + i*8));
                            } else if(j == 1){
                                tmp_wList.add("11/" + Integer.toString(today + j -1) + " " + weathers.get(j + i*8));
                            } else{
                                tmp_wList.add("11/" + Integer.toString(today + j -1) + " 降水確率：" + pops.get(j + i*8) + "% " + weathers.get(j + i*8));
                            }
                        }
                        ListView<String> tmp_wView = new ListView<>(tmp_wList);    
                        tmp_wView.setPrefSize(250, 300);
                        tmp_wView.setEditable(true);
                        tmp_wView.setItems(tmp_wList);
                        views.getChildren().addAll(tmp_wView);
                    }
                }

                

            } else {
                alert.setText("何も入力されていません");
            }

            event.consume();
        };
        searchBtn.addEventHandler(ActionEvent.ANY, searchBtnActionFilter);

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