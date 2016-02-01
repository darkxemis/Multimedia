package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

/**
 * Pantalla del juego, donde el usuario juega la partida
 * @author Santiago Faci
 * @version curso 2014-2015
 */
public class Mijuego implements Screen {

	final Drop juego;

	Texture spriteGota;
	Texture spriteCubo;
	Texture spritePiedra;
	Sound sonidoGota;
	Music musicaLluvia;
	Sound metal;
	Rectangle cubo;
	Rectangle gota;
	Rectangle piedra;
	Array<Rectangle> gotas;
	Array<Rectangle> piedras;
	long momentoUltimaGota;
	long momentoUltimaPieda;
	long gotasRecogidas;

	OrthographicCamera camara;

	public Mijuego(Drop juego) {
		this.juego = juego;

		// Carga las imágenes del juego
		spriteGota = new Texture(Gdx.files.internal("gota.png"));
		spriteCubo = new Texture(Gdx.files.internal("cubo.png"));
		spritePiedra = new Texture(Gdx.files.internal("piedra.png"));

		// Carga los sonidos del juego
		sonidoGota = Gdx.audio.newSound(Gdx.files.internal("waterdrop.wav"));
		musicaLluvia = Gdx.audio.newMusic(Gdx.files.internal("undertreeinrain.mp3"));
		metal = Gdx.audio.newSound(Gdx.files.internal("metal.mp3"));

		// Inicia la mísica de fondo del juego en modo bucle
		musicaLluvia.setLooping(true);

		// Representa el cubo en el juego
		// Hay que tener el cuenta que la imagen del cubo es de 64x64 px
		cubo = new Rectangle();
		cubo.x = 1024 / 2 - 64 / 2;
		cubo.y = 20;
		cubo.width = 64;
		cubo.height = 64;

		// Genera la lluvia
		gotas = new Array<Rectangle>();
		generarLluvia();

		// Genera las piedras
		piedras = new Array<Rectangle>();
		generarPiedras();

		camara = new OrthographicCamera();
		camara.setToOrtho(false, 1024, 768);
	}

	@Override
	public void render(float delta) {
		// Pinta el fondo de la pantalla de azul oscuro (RGB + alpha)
		Gdx.gl.glClearColor(0, 0, 0.2f, 1);
		// Limpia la pantalla
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// Actualiza la cámara
		camara.update();

		// Pinta la imágenes del juego en la pantalla
		/* setProjectionMatrix hace que el objeto utilice el
		 * sistema de coordenadas de la cámara, que
		 * es ortogonal
		 * Es recomendable pintar todos los elementos del juego
		 * entras las mismas llamadas a begin() y end()
		 */
		juego.spriteBatch.setProjectionMatrix(camara.combined);
		juego.spriteBatch.begin();
		juego.spriteBatch.draw(spriteCubo, cubo.x, cubo.y);
		for (Rectangle gota : gotas)
			juego.spriteBatch.draw(spriteGota, gota.x, gota.y);
		for (Rectangle piedra : piedras)
			juego.spriteBatch.draw(spritePiedra, piedra.x, piedra.y);
		juego.fuente.setColor(Color.RED);
		juego.fuente.draw(juego.spriteBatch, gotasRecogidas + " Puntos", 1024 - 100, 768 - 50);
		juego.spriteBatch.end();
		/*
		if (gotasRecogidas == 3) {
			musicaLluvia.stop();
			juego.setScreen(new GameOver(juego, gotasRecogidas));
		}
		*/
		/*
		 * Mueve el cubo pulsando en la pantalla
		 */
		if (Gdx.input.isTouched()) {
			Vector3 posicion = new Vector3();
			posicion.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			/*
			 * Transforma las coordenadas de la posición
			 * al sistema de coordenadas de la cámara
			 */
			camara.unproject(posicion);
			cubo.x = posicion.x - 64 /2;
		}

		/*
		 * Mueve el cubo pulsando las teclas LEFT y RIGHT
		 */
		if (Gdx.input.isKeyPressed(Keys.LEFT))
			cubo.x -= 200 * Gdx.graphics.getDeltaTime();
		if (Gdx.input.isKeyPressed(Keys.RIGHT))
			cubo.x += 200 * Gdx.graphics.getDeltaTime();

		/*
		 * Comprueba que el cubo no se salga de los
		 * límites de la pantalla
		 */
		if (cubo.x < 0)
			cubo.x = 0;
		if (cubo.x > 1024 - 64)
			cubo.x = 1024 - 64;

		/*
		 * Genera nuevas gotas dependiendo del tiempo que ha
		 * pasado desde la última
		 */
		if (TimeUtils.nanoTime() - momentoUltimaGota > 400000000)
			generarLluvia();
		if (gotasRecogidas < 10){
			if (TimeUtils.nanoTime() - momentoUltimaPieda > 700000000)
				generarPiedras();
		}else{
			if (TimeUtils.nanoTime() - momentoUltimaPieda > 100000000)
				generarPiedras();
		}

		/*
		 * Actualiza las posiciones de las gotas
		 * Si la gota llega al suelo se elimina
		 * Si la gota toca el cubo suena y se elimina
		 */
		Iterator<Rectangle> iter = gotas.iterator();
		while (iter.hasNext()) {
			gota = iter.next();
			gota.y -= 200 * Gdx.graphics.getDeltaTime();
			//if (gota.overlaps(piedra))
			//	iter.remove();
			if (gota.y + 64 < 0)
				iter.remove();
			if (gota.overlaps(cubo)) {
				sonidoGota.play();
				iter.remove();
				gotasRecogidas++;
			}
		}

		Iterator<Rectangle> itera = piedras.iterator();
		while (itera.hasNext()) {
			piedra = itera.next();
			piedra.y -= 200 * Gdx.graphics.getDeltaTime();
			if (piedra.overlaps(gota))
				itera.remove();
			if (piedra.y + 64 < 0)
				itera.remove();
			if (piedra.overlaps(cubo)) {
				itera.remove();
				musicaLluvia.stop();
				metal.play();
				juego.setScreen(new GameOver(juego, gotasRecogidas));
			}
		}
	}

	/**
	 * Genera una gota de lluvia en una posición aleatoria
	 * de la pantalla y anota el momento de generarse
	 */
	private void generarLluvia() {
		Rectangle gota = new Rectangle();
		gota.x = MathUtils.random(0, 1024 - 64);
		gota.y = 768;
		gota.width = 64;
		gota.height = 64;
		gotas.add(gota);
		momentoUltimaGota = TimeUtils.nanoTime();
	}

	private void generarPiedras() {
		Rectangle piedra = new Rectangle();
		piedra.x = MathUtils.random(0, 1024 - 64);
		piedra.y = 768;
		piedra.width = 64;
		piedra.height = 64;
		piedras.add(piedra);
		momentoUltimaPieda = TimeUtils.nanoTime();
	}

	/*
	 * Método que se invoca cuando esta pantalla es
	 * la que se está mostrando
	 * @see com.badlogic.gdx.Screen#show()
	 */
	@Override
	public void show() {
		musicaLluvia.play();
	}

	/*
	 * Método que se invoca cuando esta pantalla
	 * deja de ser la principal
	 * @see com.badlogic.gdx.Screen#hide()
	 */
	@Override
	public void hide() {
	}

	@Override
	public void dispose() {
		// Libera los recursos utilizados
		spriteGota.dispose();
		spriteCubo.dispose();
		spritePiedra.dispose();
		sonidoGota.dispose();
		musicaLluvia.dispose();
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}
}
