package com.codeoftheweb.salvo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//@Entity (sirve para crear una clase, da entidad en base de datos)
@Entity
public class Ship {
    //@Id (valor determinado para una instancia determinada)
    //@GeneratedValue(se genera de forma automática una clave en la base de datos)
    //@GenericGenerator(sirven para generar cada Id)
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")

    //DECLARACION DE VARIABLES
    private Long id;
    private String type;

    //Relacion Many to One para relacionar muchas ships a un gamePlayer
    //JoinColumn es una anotación utilizada para generar una tabla con un nombre en particular.
    //A partir del ManyToOne se crea una nueva variable "game" que sale de la clase Game.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="gamePlayer")
    private GamePlayer gamePlayer;

    //El @ElementCollection se utiliza como un OneToMany / ManyToOne que solo tenemos un atributos y no queremos crear una clase entera solo por el.
    //Esto se utiliza en salvo y ship con el atributo location
    @ElementCollection
    @Column(name="shipLocation")
    private List<String> shipLocations = new ArrayList<>();

    //CONSTRUCTOR ABIERTO
    public Ship() { }

    public Ship(String type, GamePlayer gamePlayer, List<String> shipLocations) {
        this.type = type;
        this.gamePlayer = gamePlayer;
        this.shipLocations = shipLocations;
    }

    //Constructor que me genera un objeto con las variables que le pido
    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }

    //SHIPDTO
    public Map<String,Object> makeShipDTO(){

        Map<String,Object> dto= new LinkedHashMap<>();
        dto.put("type",this.getType());
        dto.put("locations",this.getShipLocations());
        return dto;
    }

    //GETTERS Y SETTERS
    public Long getId() {
        return id;
    }

    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getShipLocations() {
        return shipLocations;
    }

    public void setShipLocations(List<String> shipLocations) {
        this.shipLocations = shipLocations;
    }
}

