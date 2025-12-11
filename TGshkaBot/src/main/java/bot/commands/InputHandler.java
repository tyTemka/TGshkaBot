package bot.commands;

@FunctionalInterface //Функциональный -> один абс метод + работа с лямбда функ
public interface InputHandler {
	void handle(String input);
}
