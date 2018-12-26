package application;

import java.io.IOException;
import java.util.Random;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class Main extends Application {

	@FXML
	private Circle ball;
	@FXML
	private Rectangle player1, player2;
	@FXML
	private AnchorPane pane;
	@FXML
	private Slider speedSlider;
	@FXML
	private Label lbl_p1_score, lbl_p2_score;

	private Scene scene;
	private AnimationTimer timer;
	private double ballX, ballY, ballR, dx, dy, recY;
	private SimpleDoubleProperty speed;
	private int player1_score, player2_score;
	private Alert alert;
	private boolean key_W, key_S, key_UP, key_DOWN, key_RIGHT, key_LEFT;

	@Override
	public void start(Stage primaryStage) {
		try {
			init_scene();
			init_timer();
			init_speed_slider();
			init_ball();
			init_players();
			init_alert();

			primaryStage.setScene(scene);
			primaryStage.getIcons().add(new Image("ping-pong.png"));
			primaryStage.setTitle("Ping Pong");
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void init_scene() throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("GameView.fxml"));
		loader.setController(this);
		scene = new Scene(loader.load());
		scene.setOnKeyPressed(e -> {
			switch (e.getCode()) {

			case W:
				key_W = true;
				break;
			case S:
				key_S = true;
				break;
			case UP:
				key_UP = true;
				break;
			case DOWN:
				key_DOWN = true;
				break;
			case RIGHT:
				speedSlider.increment();
				break;
			case LEFT:
				speedSlider.decrement();
				break;
			default:
				break;
			}
		});
		scene.setOnKeyReleased(e -> {
			switch (e.getCode()) {
			case W:
				key_W = false;
				break;
			case S:
				key_S = false;
				break;
			case UP:
				key_UP = false;
				break;
			case DOWN:
				key_DOWN = false;
			default:
				break;
			}
		});
	}

	public void init_timer() {
		timer = new AnimationTimer() {
			@Override
			public void handle(long now) {
				move_players();
				move_ball();
			}
		};
	}

	public void init_ball() {
		ball.setLayoutX(pane.getPrefWidth() / 2);
		ball.setLayoutY(pane.getPrefHeight() / 2);
		ballR = (int) ball.getRadius();
		double[] moves = { -speed.get(), speed.get() };
		Random rand = new Random();
		dx = moves[rand.nextInt(2)];
		dy = moves[rand.nextInt(2)];
	}

	public void init_speed_slider() {
		speedSlider.valueProperty().addListener((obs, oldval, newVal) -> speedSlider.setValue(newVal.intValue()));
		speed = new SimpleDoubleProperty(1.0);
		speed.bind(speedSlider.valueProperty());
		speedSlider.setValue(1.0);
	}

	public void init_players() {
		player1.setLayoutY((pane.getPrefHeight() - player1.getHeight()) / 2);
		player2.setLayoutY((pane.getPrefHeight() - player2.getHeight()) / 2);
		// set scores:
		player1_score = 0;
		player2_score = 0;
		lbl_p1_score.setText(player1_score + "");
		lbl_p2_score.setText(player2_score + "");
	}

	public void init_alert() {
		alert = new Alert(AlertType.INFORMATION, "Restart the game?", ButtonType.YES, ButtonType.NO);
		alert.setTitle("Game over");
		((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image("ping-pong.png"));
	}

	public void goUP(Rectangle player) {
		recY = player.getLayoutY();
		if (recY >= 10)
			player.setLayoutY(recY - 10);
	}

	public void goDOWN(Rectangle player) {
		recY = player.getLayoutY();
		if (recY + player.getHeight() <= pane.getHeight() - 10)
			player.setLayoutY(recY + 10);
	}

	public void change_speed() {
		if (key_RIGHT)
			speedSlider.increment();
		else if (key_LEFT)
			speedSlider.decrement();
	}

	public void move_players() {
		if (key_W)
			goUP(player1);
		if (key_S)
			goDOWN(player1);
		if (key_UP)
			goUP(player2);
		if (key_DOWN)
			goDOWN(player2);
	}

	public void move_ball() {
		ballX = ball.getLayoutX();
		ballY = ball.getLayoutY();

		// change dx and dy when the speed changes
		dx = dx > 0 ? speed.get() : -speed.get();
		dy = dy > 0 ? speed.get() : -speed.get();

		// go left when the ball hits the second player
		if (ball.getBoundsInParent().intersects(player2.getBoundsInParent())) {
			dx = -speed.get();
		}
		// go right when the ball hits the first player
		else if (ball.getBoundsInParent().intersects(player1.getBoundsInParent())) {
			dx = speed.get();
		}
		// go down
		else if (ballY - ballR <= 0.0) {
			dy = speed.get();
		}
		// go up
		else if (ballY + ballR >= pane.getHeight()) {
			dy = -speed.get();
		}

		// increase score for player 1
		else if (ballX + ballR >= pane.getWidth()) {
			player1_score++;
			lbl_p1_score.setText(player1_score + "");
			// reinitialize ball bounds:
			ballX = pane.getWidth() / 2;
			ballY = pane.getHeight() / 2;
			init_ball();
		}

		// increase score for player 2
		else if (ballX <= 0.0) {
			player2_score++;
			lbl_p2_score.setText(player2_score + "");
			// reinitialize ball bounds:
			ballX = pane.getWidth() / 2;
			ballY = pane.getHeight() / 2;
			init_ball();
		}

		// finish the game
		if (player1_score == 10) {
			timer.stop();
			alert.setHeaderText("Player 1 won");
			alert.setOnHidden(evt -> showMessage());
			alert.show();

		} else if (player2_score == 10) {
			timer.stop();
			alert.setHeaderText("Player 2 won");
			alert.setOnHidden(evt -> showMessage());
			alert.show();
		}

		ball.setLayoutX(ballX + dx);
		ball.setLayoutY(ballY + dy);
	}

	public void showMessage() {
		if (alert.getResult() == ButtonType.NO) {
			System.exit(0);
		} else {
			restartGame();
		}
	}

	public void restartGame() {
		init_ball();
		init_players();
		speedSlider.setValue(1.0);
	}

	@FXML
	public void onStart() {
		timer.start();
	}

	@FXML
	public void onPause() {
		timer.stop();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
