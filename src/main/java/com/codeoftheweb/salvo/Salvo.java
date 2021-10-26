package com.codeoftheweb.salvo;


import net.minidev.json.annotate.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//@Entity (sirve para crear una clase, da entidad en base de datos)
@Entity
public class Salvo {
    //@Id (valor determinado para una instancia determinada)
    //@GeneratedValue(se genera de forma automática una clave en la base de datos)
    //@GenericGenerator(sirven para generar cada Id)
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    //DECLARACION DE VARIABLES A USAR
    private Long id;
    private int turn;

    //Relacion Many to One para relacionar muchos salvos a un gamePlayer
    //JoinColumn es una anotación utilizada para generar una tabla con un nombre en particular.
    //A partir del ManyToOne se crea una nueva variable "gamePlayerID" que sale de la clase GamePlayer.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gamePlayerID")
    private GamePlayer gamePlayerID;

    //El @ElementCollection se utiliza como un OneToMany / ManyToOne que solo tenemos un atributos y no queremos crear una clase entera solo por el.
    //Esto se utiliza en salvo y ship con el atributo location
    @ElementCollection
    @Column(name="salvoLocations")
    private List<String> salvoLocations = new ArrayList<>();

    public Salvo() { }
    //Constructor que me genera un objeto con las variables que le pido
    public Salvo(int turn, GamePlayer gamePlayerID, List<String> salvoLocations) {
        this.turn = turn;
        this.gamePlayerID = gamePlayerID;
        this.salvoLocations = salvoLocations;
    }
    //Genero un salvo DTO que va a ser usado en el Gameplayer, el mismo me indica el turno, el player (se saca desde salvo pidiendo el ID de gameplayer, a partir
    //de ese id de gameplayer pido el player y su id
    // y por ultimo muestro las locaciones de los salvos
    public Map<String,Object> makeSalvoDTO(){

        Map<String,Object> dto= new LinkedHashMap<>();
        dto.put("turn",this.getTurn());
        dto.put("player",this.getGamePlayerID().getPlayer().getId());
        dto.put("locations",this.getSalvoLocations());
        return dto;
    }
    //GETTERS Y SETTERS
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public GamePlayer getGamePlayerID() {
        return gamePlayerID;
    }

    public void setGamePlayerID(GamePlayer gamePlayerID) {
        this.gamePlayerID = gamePlayerID;
    }

    public List<String> getSalvoLocations() {
        return salvoLocations;
    }

    public void setSalvoLocations(List<String> salvoLocations) {
        this.salvoLocations = salvoLocations;
    }

}
