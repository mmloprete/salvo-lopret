package com.codeoftheweb.salvo;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
//RestController simplifica la creacion de Restful web services
@RestController
//RequestMapping es una anotacion que se encarga de relacionar un método con una petición http en este caso la peticion sería de tipo get
//RequestMapping sin method = getmapping
@RequestMapping("/api")
//Genero la clase del controlador
public class SalvoController{

    //Con Autowired (Inyeccion de dependencias) puedo traer los repositorios y dependencias que necesite usar en mi controlador
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private GamePlayerRepository gamePlayerRepository;
    @Autowired
    private ShipRepository shipRepository;
    @Autowired
    private ScoreRepository scoreRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private SalvoRepository salvoRepository;

    //Autentication proviene de spring security y me permite verificar que yo sea un usuario existente logueado y no un usuario no existente
    private boolean isGuest(Authentication authentication) {
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
    }


    @RequestMapping("/games")
    //Genero un mapa dentro de getmapping que me permita saber si soy un usuario logueado o no haya ninguno registrado (caso Guest)
    //makeGame es el nombre del mapa
    public Map <String, Object> makeGame(Authentication authentication){
        Map<String, Object> dto= new LinkedHashMap<String,Object>();

        //caso de que no tenga un usuario me genera un guest en el mapa
        if(isGuest(authentication)){
            dto.put("player","Guest");
        }
        //caso de tener un usuario logueado me genera el nombre de la persona autenticada en el mapa
        else{
            dto.put("player",playerRepository.findByUserName(authentication.getName()).makePlayerDTO());

        }

        //coloco todos los juegos existentes en el mapa (makegameDTO) y devuelvo el mapa entero con los parametros anteriores
        dto.put("games", gameRepository.findAll()
                .stream()
                .map(game -> game.makeGameDTO())
                .collect(Collectors.toList()));
        return dto;
        //al escribir en localhost.../games me debería generar un mapa con toda esta informacion relevante
    }


    @RequestMapping("/game_view/{nn}")
    public  ResponseEntity<Map> findGamePlayer(@PathVariable Long nn,Authentication authentication) {
        GamePlayer gamePlayerID = gamePlayerRepository.findById(nn).get();
        GamePlayer enemigo=null;
        if(gamePlayerID.getGame().getGamePlayer().stream().anyMatch(b -> !b.getId().equals(gamePlayerID.getId())) )
        {enemigo =gamePlayerID.getGame().getGamePlayer().stream().filter(b -> !b.getId().equals(gamePlayerID.getId())).findFirst().get(); }

        if(playerRepository.findByUserName(authentication.getName()).getGamePlayer().stream()
                .anyMatch(b->b.getId().equals(nn))
        ){
            if(enemigo!= null){
                if (enemigo.GameState().equals("WON")) {
                    Score Score5= new Score(0D,LocalDateTime.now(),gamePlayerID.getGame(),gamePlayerID.getPlayer());
                    scoreRepository.save(Score5);
                    Score Score6= new Score(1D,LocalDateTime.now(),enemigo.getGame(),enemigo.getPlayer());
                    scoreRepository.save(Score6);
                }
                if (gamePlayerID.GameState().equals("WON")) {
                    Score Score3= new Score(1D,LocalDateTime.now(),gamePlayerID.getGame(),gamePlayerID.getPlayer());
                    scoreRepository.save(Score3);
                    Score Score4= new Score(0D,LocalDateTime.now(),enemigo.getGame(),enemigo.getPlayer());
                    scoreRepository.save(Score4);
                }
                if (gamePlayerID.GameState().equals("TIE")) {
                    Score Score1= new Score(0.5D,LocalDateTime.now(),enemigo.getGame(),enemigo.getPlayer());
                    scoreRepository.save(Score1);
                    Score Score2= new Score(0.5D,LocalDateTime.now(),gamePlayerID.getGame(),gamePlayerID.getPlayer());
                    scoreRepository.save(Score2);
                }}
            return new ResponseEntity<>(gamePlayerID.makeGameViewDTO(),HttpStatus.ACCEPTED);
        }
        else{ return new ResponseEntity<>(makeMap("Estas haciendo trampa",0),HttpStatus.UNAUTHORIZED);}
    }

    //RequestMapping de tipo post = Postmapping
    @RequestMapping(path = "/players", method = RequestMethod.POST)
    //Creo un map de responsentity que se llama create user
    //@RequestParam es capaz de leer los parámetros que adjuntemos a la url: por ej /personas?nombre=pepe y usamos @RequestParam para que
    //nos lea lo que esta adentro de nombre
    public ResponseEntity<Map<String, Object>> createUser(@RequestParam String email, @RequestParam String password) {
        //si el email esta vacío envía un status http de forbidden con un mensaje de error, no name
        if (email.isEmpty()) {
            return new ResponseEntity<>(makeMap("error", "No name"), HttpStatus.FORBIDDEN);
        }
        //si el jugador del email es distinto de nulo devuelve un response entity de error el usuario ya existe
        Player player = playerRepository.findByUserName(email);
        if (player != null) {
            return new ResponseEntity<>(makeMap("error", "Username already exists"), HttpStatus.CONFLICT);
        }
        //guarda un nuevo objeto player que me codifique la contraseña y me crea el jugador
        Player newPlayer = playerRepository.save(new Player(email,passwordEncoder.encode(password)));
        return new ResponseEntity<>(makeMap("id", newPlayer.getId()), HttpStatus.CREATED);
    }
    //devuelve un mapa con estos valores
    private Map<String, Object> makeMap(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }
    @PostMapping(path="/games")
    public  ResponseEntity<Map> findGamePlayer(Authentication authentication) {


        LocalDateTime Tiempo = LocalDateTime.now();

        Game newgame = gameRepository.save(new Game(Tiempo));
        GamePlayer newGamePlayer = gamePlayerRepository.save(new GamePlayer(this.playerRepository.findByUserName(authentication.getName()),LocalDateTime.now(),newgame));



        return new ResponseEntity<>(makeMap("gpid",newGamePlayer.getId()), HttpStatus.ACCEPTED);
    }
    @RequestMapping(value = "/game/{gameid}/players",method = {RequestMethod.GET,RequestMethod.POST})
    public  ResponseEntity<Map> joinGameButton(@PathVariable Long gameid,Authentication authentication) {

        if(playerRepository.findByUserName(authentication.getName())==null){
            System. out. println("no autorizado");

            return new ResponseEntity<>(makeMap("No esta autorizado",0), HttpStatus.UNAUTHORIZED);
        }
        if(gameRepository.findAll().stream().noneMatch(b->b.getId().equals(gameid))){
            System. out. println("No existe"+gameRepository.getById(gameid).getId());

            return new ResponseEntity<>(makeMap("No existe este juego",0), HttpStatus.FORBIDDEN);

        }

        if( gameRepository.getById(gameid).getGamePlayer().size() >=2){
            System. out. println("Cantidad:"+ gameRepository.getById(gameid).getScores().size());
            return new ResponseEntity<>(makeMap("Ya hay muchos jugadores",0), HttpStatus.FORBIDDEN);

        }

        GamePlayer newGamePlayer = gamePlayerRepository.save(new GamePlayer(
                this.playerRepository.findByUserName(authentication.getName()),LocalDateTime.now(),gameRepository.getById(gameid)
        ));
        return new ResponseEntity<>(makeMap("gpid",newGamePlayer.getId()), HttpStatus.CREATED);

    }
    @RequestMapping(value = "/games/players/{gameplayerid}/ships",method = RequestMethod.POST)
    public  ResponseEntity<Map> PlaceShips(@PathVariable Long gameplayerid, @RequestBody List<Ship> barco, Authentication authentication) {

        if (playerRepository.findByUserName(authentication.getName()) == null) {
            return new ResponseEntity<>(makeMap("No esta autorizado", 0), HttpStatus.UNAUTHORIZED);
        }
        if (gamePlayerRepository.findAll().stream().noneMatch(b -> b.getId().equals(gameplayerid))) {
            return new ResponseEntity<>(makeMap("Este Gameplayer no existe", 0), HttpStatus.FORBIDDEN);
        }
        if (playerRepository.findByUserName(authentication.getName()).getGamePlayer().stream().noneMatch(b -> b.getId().equals(gameplayerid))) {
            return new ResponseEntity<>(makeMap("Este jugador no te pertenece", 0), HttpStatus.FORBIDDEN);
        }
        for (Ship x : barco) {
            if (x.getType().equals("carrier") && x.getShipLocations().size() != 5) {
                System.out.println("no anda el 1");
                return new ResponseEntity<>(makeMap("error", "Error en el posicionamiento de barcos"), HttpStatus.FORBIDDEN);
            }
            if (x.getType().equals("submarine") && x.getShipLocations().size() != 3) {
                System.out.println("no anda el 2");
                return new ResponseEntity<>(makeMap("error", "Error en el posicionamiento de barcos"), HttpStatus.FORBIDDEN);
            }
            if (x.getType().equals("battleship") && x.getShipLocations().size() != 4) {
                System.out.println("no anda el 3");
                return new ResponseEntity<>(makeMap("error", "Error en el posicionamiento de barcos"), HttpStatus.FORBIDDEN);
            }
            if (x.getType().equals("destroyer") && x.getShipLocations().size() != 3) {
                System.out.println("no anda el 4");
                return new ResponseEntity<>(makeMap("error", "Error en el posicionamiento de barcos"), HttpStatus.FORBIDDEN);
            }
            if (x.getType().equals("patrolboat") && x.getShipLocations().size() != 2) {
                System.out.println("no anda el 5");
                return new ResponseEntity<>(makeMap("error", "Error en el posicionamiento de barcos"), HttpStatus.FORBIDDEN);
            }
            if (!x.getType().equals("patrolboat") && !x.getType().equals("destroyer") && !x.getType().equals("battleship") && !x.getType().equals("submarine") && !x.getType().equals("carrier")) {
                System.out.println("no anda el 6");

                return new ResponseEntity<>(makeMap("error", "Error en el nombre de barcos!"), HttpStatus.FORBIDDEN);
            }
        }
            barco.forEach(b -> b.setGamePlayer(gamePlayerRepository.getById(gameplayerid)));

            barco.forEach(b -> shipRepository.save(b));

            return new ResponseEntity<>(makeMap("Barcos puestos", 1), HttpStatus.CREATED);

    }


    @RequestMapping(value = "/games/players/{gameplayerid}/ships",method = RequestMethod.GET)
    public  ResponseEntity<Map> PlaceShips(@PathVariable Long gameplayerid
            ,Authentication authentication) {
        if(gamePlayerRepository.findById(gameplayerid).isPresent()){
            Map<String, Object> dto = new LinkedHashMap<>();
            dto.put("ship",gamePlayerRepository.findById(gameplayerid).get().getShip().stream().map(b -> b.makeShipDTO()).collect(Collectors.toList()));

            return new ResponseEntity<>(dto, HttpStatus.CREATED);             }
        return new ResponseEntity<>(makeMap("Este gameplayer no existe",0), HttpStatus.FORBIDDEN);
    }


    @RequestMapping(value = "/games/players/{gameplayerID}/salvoes",method = RequestMethod.POST)
    public ResponseEntity<Map> disparos(@PathVariable Long gameplayerID,@RequestBody Salvo salvoLocations,Authentication authentication){

        if(playerRepository.findByUserName(authentication.getName()).equals(null)){
            return new ResponseEntity<>(makeMap("No se encontró el usuario",0), HttpStatus.UNAUTHORIZED);
        }
        if(gamePlayerRepository.findAll().stream().noneMatch(a->a.getId().equals(gameplayerID))){
            return new ResponseEntity<>(makeMap("no hay gameplayer con los datos de id ingresados en la url",0), HttpStatus.UNAUTHORIZED);
        }
        if(playerRepository.findByUserName(authentication.getName()).getGamePlayer().stream().noneMatch(a->a.getId().equals(gameplayerID))) {
            return new ResponseEntity<>(makeMap("El gameplayer con el nombre de usuario dado no existe", 0), HttpStatus.UNAUTHORIZED);
        }
        if(gamePlayerRepository.getById(gameplayerID).getGame().getGamePlayer().size()!=2){
            return new ResponseEntity<>(makeMap("No hay ningún oponente", 0),HttpStatus.UNAUTHORIZED);
        }
        /*
        if(gamePlayerRepository.getById(gameplayerID).getGame().getGamePlayer().stream().filter(b->!b.getId().equals(gameplayerID)).findFirst().isPresent()){
            return new ResponseEntity<>(makeMap("El oponente no puso barcos",0),HttpStatus.UNAUTHORIZED);
        }
*/
        int max = 0;
        if(gamePlayerRepository.getById(gameplayerID).getSalvos().size()==0)
        {
            max=1;
        }
        else {
            max = Collections.max(gamePlayerRepository.getById(gameplayerID).getSalvos().stream().map(b -> b.getTurn()).collect(Collectors.toList()));
            max=max+1;
        }
        GamePlayer Oponente=gamePlayerRepository.getById(gameplayerID).getGame()
                .getGamePlayer().stream().filter(b->!b.getId().equals(gameplayerID)).findFirst().get();

        GamePlayer Jugador=gamePlayerRepository.getById(gameplayerID);

        int EnemySalvo= Oponente.getSalvos().size();
        int MySalvo= Jugador.getSalvos().size();

        if(Oponente.getId()<Jugador.getId()){
            if(Oponente.getSalvos().size()<=Jugador.getSalvos().size()){
                return new ResponseEntity<>(makeMap("error","No es tu turno"), HttpStatus.FORBIDDEN);
            }
            else {
                salvoLocations.setTurn(max);
                salvoLocations.setGamePlayerID(Jugador);
                salvoRepository.save(salvoLocations);
                return new ResponseEntity<>(makeMap("OK","tiro hecho"), HttpStatus.CREATED);
            }
        }
        if(Oponente.getId()>Jugador.getId()){
            if(Oponente.getSalvos().size()>=Jugador.getSalvos().size()){
                salvoLocations.setTurn(max);
                salvoLocations.setGamePlayerID(Jugador);
                salvoRepository.save(salvoLocations);
                return new ResponseEntity<>(makeMap("OK","tiro hecho"), HttpStatus.CREATED);
            }
            else {
                return new ResponseEntity<>(makeMap("error","No es tu turno"), HttpStatus.FORBIDDEN);
            }
        }
        return new ResponseEntity<>(makeMap("error","problema"), HttpStatus.FORBIDDEN);
    }
}