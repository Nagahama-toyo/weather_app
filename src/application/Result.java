package application;

import javafx.beans.property.SimpleStringProperty;

public class Result {
    SimpleStringProperty weather;
    SimpleStringProperty pop;

    Result(String wth, String p){
        this.weather = new SimpleStringProperty(wth);
        this.pop = new SimpleStringProperty(p);
    }

    public String getWeather(){
        return weather.get();
    }

    public void setWeather(String wth){
        weather.set(wth);
    }

    public String getPOP(){
        return pop.get();
    }

    public void setPOP(String p){
        weather.set(p);
    }


    
}
