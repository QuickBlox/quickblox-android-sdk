package com.quickblox.android.framework.snippets.modules;

import android.content.Context;
import com.quickblox.android.framework.snippets.Snippet;
import com.quickblox.android.framework.snippets.Snippets;
import com.quickblox.core.QBCallback;
import com.quickblox.core.result.Result;
import com.quickblox.module.ratings.QBRatings;
import com.quickblox.module.ratings.model.QBGameMode;
import com.quickblox.module.ratings.model.QBScore;
import com.quickblox.module.ratings.result.QBGameModeResult;

/**
 * User: Oleg Soroka
 * Date: 11.10.12
 * Time: 15:27
 */
public class SnippetsRatings extends Snippets {

    public SnippetsRatings(Context context) {
        super(context);

        snippets.add(createGameMode);
        snippets.add(getGameMode);
        snippets.add(deleteGameMode);
        snippets.add(createScore);
        snippets.add(getScore);
        snippets.add(deleteScore);
    }

    int gameModeId = 0;
    int scoreId = 0;

    Snippet createGameMode = new Snippet("create game mode") {
        @Override
        public void execute() {
            QBGameMode gameMode = new QBGameMode("my game mode");
            QBRatings.createGameMode(gameMode, new QBCallback() {
                @Override
                public void onComplete(Result result) {
                    printResultToConsole(result);

                    if (result.isSuccess()) {
                        QBGameModeResult gameModeResult = (QBGameModeResult) result;
                        QBGameMode newGameMode = gameModeResult.getGameMode();

                        System.out.println(">>> new game mode is:" + newGameMode);

                        gameModeId = newGameMode.getId();
                    }
                }
            });
        }
    };

    Snippet getGameMode = new Snippet("get game mode") {
        @Override
        public void execute() {
            if (gameModeId != 0) {
                QBGameMode gameMode = new QBGameMode(gameModeId);

                QBRatings.getGameMode(gameMode, new QBCallback() {
                    @Override
                    public void onComplete(Result result) {
                        printResultToConsole(result);
                    }
                });

            } else {
                System.out.println("Create Game Mode before retrieving.");
            }
        }
    };

    Snippet deleteGameMode = new Snippet("delete game mode") {
        @Override
        public void execute() {
            if (gameModeId != 0) {
                QBGameMode gameMode = new QBGameMode(gameModeId);

                QBRatings.deleteGameMode(gameMode, new QBCallback() {
                    @Override
                    public void onComplete(Result result) {
                        printResultToConsole(result);
                    }
                });

            } else {
                System.out.println("Create Game Mode before deleting.");
            }
        }
    };

    Snippet createScore = new Snippet("create score") {
        @Override
        public void execute() {
            if (gameModeId != 0) {
                QBScore score = new QBScore();
                score.setGameModeId(gameModeId);
                score.setValue(100500);

                QBRatings.createScore(score, new QBCallback() {
                    @Override
                    public void onComplete(Result result) {
                        printResultToConsole(result);
                    }
                });
            } else {
                System.out.println("Create Game Mode before create score.");
            }
        }
    };

    Snippet getScore = new Snippet("get score") {
        @Override
        public void execute() {
            if (scoreId != 0) {
                QBScore score = new QBScore(scoreId);

                QBRatings.getScore(score, new QBCallback() {
                    @Override
                    public void onComplete(Result result) {
                        printResultToConsole(result);
                    }
                });
            } else {
                System.out.println("Create Score before retrieving.");
            }
        }
    };

    Snippet deleteScore = new Snippet("delete score") {
        @Override
        public void execute() {
            if (scoreId != 0) {
                QBScore score = new QBScore(scoreId);

                QBRatings.deleteScore(score, new QBCallback() {
                    @Override
                    public void onComplete(Result result) {
                        printResultToConsole(result);
                    }
                });
            } else {
                System.out.println("Create Score before deleting.");
            }
        }
    };
}