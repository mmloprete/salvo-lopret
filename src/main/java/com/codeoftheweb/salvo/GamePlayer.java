package com.codeoftheweb.salvo;


import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

//@Entity (sirve para crear una clase, da entidad en base de datos)
@Entity
public class GamePlayer {
    //@Id (valor determinado para una instancia determinada)
    //@GeneratedValue(se genera de forma automática una clave en la base de datos)
    //@GenericGenerator(sirven para generar cada Id)
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")

    //DECLARO LAS VARIABLES A USAR
    private Long id;

    //Many to one me hace una relacion de muchos a uno, para que varios juegos puedan vincularse a un gamePlayer (En este caso)
    //JoinColumn es una anotación utilizada para generar una tabla con un nombre en particular.
    //A partir del ManyToOne se crea una nueva variable "gameID" que sale de la clase Game.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gameID")
    private Game gameID;

    //Otro many to one, este lo que hace es que varios jugadores puedan vincularse a un gameplayer
    //JoinColumn es una anotación utilizada para generar una tabla con un nombre en particular
    // A partir del ManyToOne se crea una nueva variable "PlayerID" que sale de la clase Player
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "playerID")
    private Player playerID;

    //Relacion uno a muchos que me permite vincular una variable gameplayer a muchos ships
    @OneToMany(mappedBy = "gamePlayer", fetch = FetchType.EAGER)
    private Set<Ship> ships;
    //Relacion uno a muchos que me permite vincular una variable gameplayer a muchos salvos
    @OneToMany(mappedBy = "gamePlayerID", fetch = FetchType.EAGER)
    private Set<Salvo> salvos;

    //El @ElementCollection se utiliza como un OneToMany / ManyToOne que solo tenemos un atributos y no queremos crear una clase entera solo por el.
    //Esto se utiliza en salvo y ship con el atributo location
    @ElementCollection
    @Column(name = "hits")
    private List<String> self = new ArrayList<>();

    @ElementCollection
    @Column(name = "opponenthits")
    private List<String> opponent = new ArrayList<>();


    private LocalDateTime joinDate;


    public GamePlayer() {
    }

    //Genero el constructor para poder generar un objeto con dichas variables
    public GamePlayer(Player player, LocalDateTime joinDate, Game game) {
        this.playerID = player;
        this.joinDate = joinDate;
        this.gameID = game;
    }

    //Genero un mapa o DTO de gameplayer (se ve en JSON)
    public Map<String, Object> makeGamePlayerDTO() {

        Map<String, Object> dto = new LinkedHashMap<>();
        //Obtengo el ID de gameplayer
        //Obtengo el jugador y genero el DTO creado en player (no se usa stream ni .collect ya que no es una lista que pueda recorrer)
        //Obtengo cada ship y los ordeno en lista, tambien genero un mapa con las ships (Ver en la clase Ship)
        //.stream actua como un for, es decir, un bucle de items.
        // El collect se utiliza para ordenarlos en lista y darle un inicio y final juntando cada uno de los atributos.
        dto.put("id", this.getId());
        dto.put("player", this.getPlayer().makePlayerDTO());
        dto.put("ship", this.getShip()
                .stream()
                .map(ship -> ship.makeShipDTO())
                .collect(Collectors.toList()));
        return dto;
    }


    //Genero un GameViewDTO
    //Este me da el ID del juego, la fecha de creacion del juego, el o los gameplayer asociados con el juego, el estado del juego
    // Las ships de cada Gameplayer, los salvos y los hits
    public Map<String, Object> makeGameViewDTO() {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", this.getGame().getId());
        dto.put("created", this.getGame().getCreationDate());
        dto.put("gameState", this.GameState());
        dto.put("gamePlayers", this.getGame().getGamePlayer()
                .stream()
                .map(gamePlayer -> gamePlayer.makeGamePlayerDTO())
                .collect(Collectors.toList()));
        dto.put("ships", this.getShip()
                .stream()
                .map(ship -> ship.makeShipDTO())
                .collect(Collectors.toList()));
        dto.put("salvoes", this.getGame().getGamePlayer()
                .stream()
                //flatmap me permite unir Listas
                .flatMap(gamePlayer -> gamePlayer.getSalvos()
                        .stream()
                        .map(salvo -> salvo.makeSalvoDTO()))
                .collect(Collectors.toList()));
        dto.put("hits", this.makeHitsDTO());
        return dto;
    }

    //Get y Set son simples métodos que usamos en las clases para mostrar (get) o modificar (set) el valor de un atributo.
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Game getGame() {
        return gameID;
    }

    public void setGame(Game game) {
        this.gameID = game;
    }

    public Player getPlayer() {
        return playerID;
    }

    public void setPlayerID(Player player) {
        this.playerID = player;
    }

    public LocalDateTime getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(LocalDateTime joinDate) {
        this.joinDate = joinDate;
    }

    public Set<Ship> getShip() {
        return ships;
    }

    public void setShip(Set<Ship> ship) {
        this.ships = ship;
    }

    public Set<Salvo> getSalvos() {
        return salvos;
    }

    public void setSalvos(Set<Salvo> salvos) {
        this.salvos = salvos;
    }

    public Optional<Score> getScore() {

        return this.getPlayer().getScore(gameID);

    }
    //OBTENGO LOS BARCOS HUNDIDOS
    //BOOLEAN ME DEVUELVE TRUE O FALSE
    private boolean barcosHundidos(GamePlayer gpBarcos, GamePlayer gpSalvos) {

        if (!gpBarcos.getShip().isEmpty() && !gpSalvos.getSalvos().isEmpty()) {
            return  gpSalvos.getSalvos().stream().flatMap(salvo -> salvo.getSalvoLocations()
                            .stream()).collect(Collectors.toList())
                    .containsAll(gpBarcos.getShip()
                            .stream().flatMap(ship -> ship.getShipLocations().stream())
                            .collect(Collectors.toList()));
        }
        return false;
    }

    //GENERO GAMESTATE CON SUS CONDICIONES PARA PODER CAMBIAR EL ESTADO DEL JUEGO
    public String GameState() {

        if (this.getGame().getGamePlayer().size() != 2) {
            return "WAITINGFOROPP";
        }
        GamePlayer enemigo = this.getGame().getGamePlayer().stream().filter(b -> !b.getId().equals(this.getId())).findFirst().get();

        if (this.getShip().size() != 5) {
            return "PLACESHIPS";
        }

        if (enemigo.getShip().size() != 5) {
            return "WAITINGFOROPP";
        }

        if ((this.getSalvos().size()>2 && enemigo.getSalvos().size()>2 )&&(this.getSalvos().size()== enemigo.getSalvos().size() )){
            String plygana="";
            String enegana="";

            if (barcosHundidos(this,enemigo)) {
                enegana = "Ganó";
            }
            if (barcosHundidos(enemigo,this)) {
                plygana = "Ganó";
            }
            if (plygana.equals("Ganó") && enegana.equals("Ganó")) {
                return "TIE";
            }
            if (plygana.equals("Ganó")) {
                return "WON";
            }
            if (enegana.equals("Ganó")) {
                return "LOST";
            }
        }
        if(enemigo.getId()<this.getId()){
            if(enemigo.getSalvos().size()<=this.getSalvos().size()){return "WAIT";}
            else {return "PLAY";}
        }
        if(enemigo.getId()>this.getId()){
            if(enemigo.getSalvos().size()>=this.getSalvos().size()){return "PLAY";}
            else {return "WAIT";}
        }

        return "UNDEFINED";
    }

    //DECLARO VARIABLES Y CONTADORES
    int carrier=0;
    int submarine=0;
    int battleship=0;
    int destroyer=0;
    int patrolboat=0;

    //DAMAGESDTO
    public Map<String,Object> makeDamagesDTO(int turnos, GamePlayer enemigo,int funcion){
        ArrayList<String> hits_pegados = new ArrayList<String>();
        Map<String,Object> dto= new LinkedHashMap<>();

        int carrierHits=0;
        int submarineHits=0;
        int battleshipHits=0;
        int destroyerHits=0;
        int patrolboatHits=0;

        //DTO DE TURNO Y DAÑOS
        Map<String, Object> DTOturno= new LinkedHashMap<>();
        Map<String, Object> DTOdaños  = new LinkedHashMap<>();

        //CONDICIONES DE IMPACTO Y TURNOS
        int finalTurnos = turnos;
        Salvo Salvosimport;
        if( enemigo.getSalvos()
                .stream()
                .filter(b->b.getTurn() == finalTurnos)
                .findFirst().isPresent()){
            Salvosimport = enemigo.getSalvos()
                    .stream()
                    .filter(b->b.getTurn() == finalTurnos)
                    .findFirst()
                    .get();}
        else { Salvosimport=enemigo.getSalvos()
                .stream()
                .filter(b->b.getTurn() == finalTurnos-1)
                .findFirst()
                .get();
        }

        List<String> Salvoturno= Salvosimport.getSalvoLocations();

        for (String x : this.getShip().stream().map(Ship::getType).collect(Collectors.toList())) {

            List<String> barcos = (this.getShip().stream().filter(b ->b.getType().equals(x)).findFirst().get()).getShipLocations();

            for (String b : barcos) {
                DTOturno.put("turn",finalTurnos);
                if (Salvoturno.contains(b)) {
                    hits_pegados.add(b);
                    if (x.equals("carrier")) {
                        carrier++;
                        carrierHits++;
                    }
                    if (x.equals("submarine")) {
                        submarine++;
                        submarineHits++;
                    }
                    if (x.equals("battleship")) {
                        battleship++;
                        battleshipHits++;
                    }
                    if (x.equals("destroyer")) {
                        destroyer++;
                        destroyerHits++;
                    }
                    if (x.equals("patrolboat")) {
                        patrolboat++;
                        patrolboatHits++;

                    }


                }


            }
            //GENERO MAS DTOS
            DTOturno.put("hitLocations",hits_pegados);
            DTOdaños.put("patrolboatHits",patrolboatHits);
            DTOdaños.put("destroyerHits",destroyerHits);
            DTOdaños.put("carrierHits",carrierHits);
            DTOdaños.put("submarineHits",submarineHits);
            DTOdaños.put("battleshipHits",battleshipHits);

            DTOdaños.put("carrier",carrier);
            DTOdaños.put("submarine",submarine);
            DTOdaños.put("battleship",battleship);
            DTOdaños.put("destroyer",destroyer);
            DTOdaños.put("patrolboat",patrolboat);

            DTOturno.put("damages",DTOdaños);

            DTOturno.put("missed",Salvoturno.size()-hits_pegados.size());

        }
        Ship carrier = this.getShip().stream().filter(sh -> sh.getType().equals("carrier")).findFirst().get();
        List <String> carrierLocations = carrier.getShipLocations();
        if(funcion==1){return DTOdaños;}
        else{return DTOturno;}
    }
    //LISTA DE GOLPES
    public List<Object> Golpes(){
        carrier=0;
        submarine=0;
        battleship=0;
        destroyer=0;
        patrolboat=0;

        GamePlayer enemigo=this.getGame().getGamePlayer().stream().filter(b-> !b.getId().equals(this.getId())).findFirst().get();


        ArrayList<Object> dto = new ArrayList<Object>();
        int turnos=0;
        for(turnos=1;turnos<=enemigo.getSalvos().size();turnos++) {
            dto.add(this.makeDamagesDTO(turnos,enemigo,0));
        }

        return dto;

    }
    //HITSDTO
    public Map<String, Object>makeHitsDTO(){
        Map<String, Object> dto= new LinkedHashMap<>();
        List<String> SelfHits;

        GamePlayer enemigo=null;
        if(this.getGame().getGamePlayer().stream().anyMatch(b -> !b.getId().equals(this.getId())) )
        {enemigo =this.getGame().getGamePlayer().stream().filter(b -> !b.getId().equals(this.getId())).findFirst().get(); }

        ArrayList<Object> error1 = new ArrayList<Object>();
        ArrayList<Object> error2 = new ArrayList<Object>();
        if(enemigo==null){dto.put("self",error1);
            dto.put("opponent",error2);}
        else{
            dto.put("self",this.Golpes());
            dto.put("opponent", enemigo.Golpes());}


        return dto;}


}
