package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Arrays;
//Necesario para generar un Application
@SpringBootApplication
public class SalvoApplication {
	//public static void main (String[] args) El array array args que aparece como argumento del método main
	// es el encargado de recoger y almacenar estos valores. Como args es un array de Strings contendrá cada uno de estos valores como un String
	// SpringApplication.run(SalvoApplication.class, args); lo que hace es permitirle pasar a args una clase
	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);
	}
//Un JavaBean o bean es un componente hecho en software que se puede reutilizar y que puede ser manipulado
// visualmente por una herramienta de programación en lenguaje Java.
	@Bean
	public CommandLineRunner initData(PlayerRepository playerRepository, GameRepository gameRepository,GamePlayerRepository gamePlayerRepository,ShipRepository shipRepository,SalvoRepository salvoRepository,ScoreRepository scoreRepository) {
		return (args) -> {
	//Vinculo los repositorios a usar, genero a partir de los constructores los distintos objetos, que se almacenan en los repositorios
			Player player1 = new Player("c.obrian@ctu.gov",passwordEncoder().encode("42"));
			Player player2 = new Player("j.bauer@ctu.gov",passwordEncoder().encode("24"));
			Player player3 = new Player("t.almeida@ctu.gov",passwordEncoder().encode("kb"));
			Player player4 = new Player("kim_bauer@gmail.com",passwordEncoder().encode("mole"));

			playerRepository.save(player1);
			playerRepository.save(player2);
			playerRepository.save(player3);
			playerRepository.save(player4);

			Game game1 =new Game(LocalDateTime.now());
			Game game2 =new Game(LocalDateTime.now().plusHours(1));
			Game game3 =new Game(LocalDateTime.now().plusHours(2));
			Game game4 =new Game(LocalDateTime.now().plusHours(3));
			Game game5 =new Game(LocalDateTime.now().plusHours(4));

			gameRepository.save(game1);
			gameRepository.save(game2);
			gameRepository.save(game3);
			gameRepository.save(game4);
			gameRepository.save(game5);

			GamePlayer gamePlayer1 = new GamePlayer(player1,LocalDateTime.now(),game1);
			GamePlayer gamePlayer2 = new GamePlayer(player2,LocalDateTime.now(),game1);
			GamePlayer gamePlayer3 = new GamePlayer(player1,LocalDateTime.now(),game2);
			GamePlayer gamePlayer4 = new GamePlayer(player3,LocalDateTime.now(),game2);
			GamePlayer gamePlayer5 = new GamePlayer(player2,LocalDateTime.now(),game3);
			GamePlayer gamePlayer6 = new GamePlayer(player4,LocalDateTime.now(),game3);
			GamePlayer gamePlayer7 = new GamePlayer(player2,LocalDateTime.now(),game4);
			GamePlayer gamePlayer8 = new GamePlayer(player3,LocalDateTime.now(),game4);
			GamePlayer gamePlayer9 = new GamePlayer(player1,LocalDateTime.now(),game5);

			gamePlayerRepository.save(gamePlayer1);
			gamePlayerRepository.save(gamePlayer2);
			gamePlayerRepository.save(gamePlayer3);
			gamePlayerRepository.save(gamePlayer4);
			gamePlayerRepository.save(gamePlayer5);
			gamePlayerRepository.save(gamePlayer6);
			gamePlayerRepository.save(gamePlayer7);
			gamePlayerRepository.save(gamePlayer8);
			gamePlayerRepository.save(gamePlayer9);

			Ship ship1 = new Ship("Carrier",gamePlayer1, Arrays.asList("A1","A2","A3","A4","A5"));
			shipRepository.save(ship1);

			Ship ship2 = new Ship("Battleship ",gamePlayer2, Arrays.asList("B1","B2","B3","B4"));
			shipRepository.save(ship2);

			Ship ship3 = new Ship("Submarine",gamePlayer1,Arrays.asList("C1","C2","C3"));
			shipRepository.save(ship3);

			Ship ship4 = new Ship("Destroyer",gamePlayer2,Arrays.asList("D1","D2","D3"));
			shipRepository.save(ship4);

			Ship ship5 = new Ship("Patrol Boat",gamePlayer2,Arrays.asList("E1","E2"));
			shipRepository.save(ship5);

//			Salvo salvo1 = new Salvo(1L,gamePlayer1,Arrays.asList("B8","B9"));
//			salvoRepository.save(salvo1);
//
//			Salvo salvo2 = new Salvo(2L,gamePlayer1,Arrays.asList("A4","A5"));
//			salvoRepository.save(salvo2);

			Score score1 = new Score(1D,LocalDateTime.now(),game1,player1);
			scoreRepository.save(score1);

			Score score2 = new Score(1D,LocalDateTime.now(),game1,player2);
			scoreRepository.save(score2);

		};
	}
	//PasswordEncoder nos permite codificar mensajes
	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}
}
//Configuration aclara que se van a usar mas de 2 beans
@Configuration
class WebSecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {

//@Autowired permite inyectar dependencias
	@Autowired
	PlayerRepository playerRepository;

	@Override
	public void init(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(inputName-> {
			//Al llamar al repositorio, aca lo que hacemos es encontrar el nombre de ese usario
			//Si el nombre es distinto de null, x lo que hay un nombre reconocido genera un nuevo objeto
			//Usuario y lo reconoce como user. Si el nombre esta vacío devuelve que no encontró el usuario
			Player Name = playerRepository.findByUserName(inputName);
			if (Name != null ) {
				return new User(Name.getUserName(), Name.getPassword(),
						AuthorityUtils.createAuthorityList("USER"));
			} else {
				throw new UsernameNotFoundException("Unknown user: " + inputName);
			}
		});


	}
}

@Configuration
//Habilito la seguridad web que nos ofrece spring
@EnableWebSecurity
class




WebSecurityConfig extends WebSecurityConfigurerAdapter {
	//traigo la dependencia passwordencoder para poder usarla
	@Autowired
	PasswordEncoder passwordEncoder;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
		//Asigno los permisos que va a tener cada tipo de usuario
				.antMatchers("/api/players").permitAll()
				.antMatchers("/api/login").permitAll()
				.antMatchers("/login").permitAll()
				.antMatchers("/web/**").permitAll()
				.antMatchers("/api/games").permitAll()
				.antMatchers("**").hasAuthority("USER")

		;
		//Asigno parametros de login y logout
		http.formLogin()
				.usernameParameter("name")
				.passwordParameter("pwd")
				.loginPage("/api/login");

		http.logout().logoutUrl("/api/logout");



		// turn off checking for CSRF tokens
		http.csrf().disable();

		// if user is not authenticated, just send an authentication failure response
		http.exceptionHandling().authenticationEntryPoint((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

		// if login is successful, just clear the flags asking for authentication
		http.formLogin().successHandler((req, res, auth) -> clearAuthenticationAttributes(req));

		// if login fails, just send an authentication failure response
		http.formLogin().failureHandler((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

		// if logout is successful, just send a success response
		http.logout().logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());
	}

	private void clearAuthenticationAttributes(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
		}
	}
}
