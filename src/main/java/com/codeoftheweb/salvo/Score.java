package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

//@Entity (sirve para crear una clase, da entidad en base de datos)
@Entity
public class Score {
    //@Id (valor determinado para una instancia determinada)
    //@GeneratedValue(se genera de forma automática una clave en la base de datos)
    //@GenericGenerator(sirven para generar cada Id)
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    //DECLARACION DE VARIABLES
    private Long id;

    private Double score;

    private LocalDateTime finishDate;

    //Relacion Many to One para relacionar muchos scores a un gamePlayer
    //JoinColumn es una anotación utilizada para generar una tabla con un nombre en particular.
    //A partir del ManyToOne se crea una nueva variable "game" que sale de la clase Game.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="game")
    private Game game;

    //Relacion Many to One para relacionar muchos scores a un player
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="player")
    private Player player;

    //CONSTRUCTOR ABIERTO
    public Score(){}

    //Constructor que me genera un objeto con las variables que le pido
    public Score(Double score, LocalDateTime finishDate, Game game, Player player) {
        this.score = score;
        this.finishDate = finishDate;
        this.game = game;
        this.player = player;
    }

    //GETTERS Y SETTERS
    public Double getScore() {return score;}

    public void setScore(Double score) {
        this.score = score;
    }

    public LocalDateTime getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(LocalDateTime finishDate) {
        this.finishDate = finishDate;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    //DTO DE SCORE
    public Map<String, Object> makeScoreDTO(){

        Map <String, Object> dto=   new LinkedHashMap<>();
        dto.put("game",this.getGame().getId());
        dto.put("player",this.getPlayer().getId());
        dto.put("finishDate",this.getFinishDate());
        dto.put("score",this.getScore());
        return dto;
    }

}