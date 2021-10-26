
package com.codeoftheweb.salvo;


import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

import java.util.*;
import java.util.stream.Collectors;

//@Entity (sirve para crear una clase, da entidad en base de datos)
@Entity
public class Player {
    //@Id (valor determinado para una instancia determinada)
    //@GeneratedValue(se genera de forma automática una clave en la base de datos)
    //@GenericGenerator(sirven para generar cada Id)
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    //DECLARACION DE VARIABLES
    private Long id;
    private String userName;
    private String password;
    //Relacion uno a muchos que me permite vincular una variable player a muchos gameplayer
    @OneToMany(mappedBy = "playerID", fetch = FetchType.EAGER)
    private Set<GamePlayer> gamePlayer;
    //Relacion uno a muchos que me permite vincular una variable player a muchos scores
    @OneToMany(mappedBy = "player", fetch = FetchType.EAGER)
    private Set<Score> scores;

    public Player() {
    }
    // Genero un DTO para player, este me muestra el ID del jugador y el email del mismo
    public Map<String, Object> makePlayerDTO() {

        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", this.getId());
        dto.put("email", this.getUserName());
        return dto;
    }

    // @JsonIgnore
  /*  public List<Game> getGame() {
        return gamePlayer.stream().map(x -> x.getGame()).collect(Collectors.toList());
    }
*/

    //Constructor que me permite generar objetos
    public Player(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    //GETTERS Y SETTERS

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Set<GamePlayer> getGamePlayer() {
        return gamePlayer;
    }

    public void setGamePlayer(Set<GamePlayer> gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    public Set<Score> getScore() {
        return scores;
    }

    public void setScore(Set<Score> score) {
        this.scores = score;
    }

    //Un Optional es una clase que puede o no contener un valor, es decir, que se comporta como un wrapper para cualquier tipo de objeto que pueda o no ser nulo
    public Optional<Score> getScore(Game juego){
        return this
                //Obtiene la lista de puntajes
                //Los stremea
                //filtra, saca el id del juego y si es igual al juego que le pasamos desde game encuentra el primero
                .getScore()
                .stream()
                .filter(b ->b.getGame().getId().equals(juego.getId())).findFirst();

    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}