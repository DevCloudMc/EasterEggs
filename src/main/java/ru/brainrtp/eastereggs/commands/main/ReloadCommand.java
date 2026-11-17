package ru.brainrtp.eastereggs.commands.main;

import org.bukkit.command.CommandSender;
import ru.brainrtp.eastereggs.EasterEggs;
import ru.brainrtp.eastereggs.commands.Command;
import ru.brainrtp.eastereggs.configuration.Configuration;
import ru.brainrtp.eastereggs.configuration.Language;

public class ReloadCommand extends Command {

  private final Language language;
  private final Configuration mainConfiguration;

  public ReloadCommand(Language language, Configuration mainConfiguration) {
    this.language = language;
    this.mainConfiguration = mainConfiguration;
  }

  @Override
  public void execute(CommandSender sender, String[] args) {
    try {
      mainConfiguration.load();
      language.reload("lang_" + mainConfiguration.get().node("languageFile").getString());
      EasterEggs.getEggService().getEditor().endAllSessions();
      EasterEggs.getEggService().reloadAllEggs();
    } catch (Exception e) {
      EasterEggs.getPlugin()
          .getSLF4JLogger()
          .error("Error while reloading plugin: {}", e.getMessage(), e);
      sender.sendMessage(language.getSingleMessage("reload", "failed"));
      return;
    }
    sender.sendMessage(language.getSingleMessage("reload", "success"));
  }
}
