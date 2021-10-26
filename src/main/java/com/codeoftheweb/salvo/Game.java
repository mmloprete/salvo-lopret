package com.codeoftheweb.salvo;


import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

//@Entity (sirve para crear una clase, da entidad en base de datos)
@Entity
public class Game {
    //@Id (valor determinado para una instancia determinada)
    //@GeneratedValue(se genera de forma automática una clave en la base de datos)
    //@GenericGenerator(sirven para generar cada Id)
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")

    //DECLARO LAS VARIABLES A USAR

    private Long id;
    private LocalDateTime creationDate;

    // One to many es un tipo de relacion, esta particularmente, sirve para relacionar un juego con varios gameplayers, asi dos jugadores podrían conectarse
    // El mappedBy indica el lado de la unificacion que no es la principal, siendo fetch una estructura predeterminada
    // En el Set se indica una variable para la clase la cual se utiliza como punto de referencia

    @OneToMany(mappedBy="gameID", fetch=FetchType.EAGER)
    private Set<GamePlayer> gamePlayer;

    // Este One to many, me relaciona un juego con muchos puntajes

    @OneToMany(mappedBy="game", fetch=FetchType.EAGER)
    private Set<Score> scores;

    //Genero un mapa o DTO con las variables necesarias para que aparezcan en el JSON correspondiente

    public Map <String, Object> makeGameDTO(){
        Map <String, Object> dto=   new LinkedHashMap<>();
        dto.put("id",   this.getId());

        //ID del juego
        dto.put("created", this.getCreationDate());

        //Fecha de creacion

        // En este dto llamo a otro DTO para meterlo dentro de "gamePlayers", funciona como subdivision

        dto.put("gamePlayers", this.getGamePlayer()
                .stream()

                .map(gamePlayer -> gamePlayer.makeGamePlayerDTO())

                .collect(Collectors.toList()));

        //.stream actua como un for, es decir, un bucle de items.
        // El collect se utiliza para ordenarlos en lista y darle un inicio y final juntando cada uno de los atributos.

        dto.put("scores", this
                .getGamePlayer()
                .stream()
                .map(Astro ->
                        {

                            //Astro es un nombre, si existe un score
                            if(Astro.getScore().isPresent()){

                                //devuelve un dto dentro de Score
                                return Astro.getScore().get().makeScoreDTO();

                            }
                            else
                            //Sino, me devuelve un string vacío
                            {return "";}
                        }
                ));

        return dto;
    }
    //La declaracion de clase vacía es necesaria siempre para no trabar el programa
    public Game () { }

    //Constructor, al llamar a game en salvo application, me generará un objeto con los parametros aclarados
    public Game (LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }


    //Get y Set son simples métodos que usamos en las clases para mostrar (get) o modificar (set) el valor de un atributo.
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public Set<GamePlayer> getGamePlayer() {
        return gamePlayer;
    }

    public void setGamePlayer(Set<GamePlayer> gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    public Set<Score> getScores() {
        return scores;
    }

    public void setScores(Set<Score> scores) {
        this.scores = scores;
    }
}