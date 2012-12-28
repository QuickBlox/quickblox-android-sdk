package com.quickblox.snippets.modules;

import android.content.Context;
import com.quickblox.core.QBCallback;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.internal.core.request.QBPagedRequestBuilder;
import com.quickblox.module.ratings.QBRatings;
import com.quickblox.module.ratings.model.QBGameMode;
import com.quickblox.module.ratings.model.QBScore;
import com.quickblox.module.ratings.result.*;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.snippets.Snippet;
import com.quickblox.snippets.Snippets;

/**
 * User: Oleg Soroka
 * Date: 11.10.12
 * Time: 15:27
 */
public class SnippetsRatings extends Snippets {

    public SnippetsRatings(Context context) {
        super(context);

        snippets.add(createGameMode);
        snippets.add(getGameModeWithId);
        snippets.add(deleteGameModeWithId);
        snippets.add(createScore);
        snippets.add(getScoreWithId);
        snippets.add(deleteScoreWithId);
        snippets.add(getGameModes);
        snippets.add(updateGameMode);
        snippets.add(updateScore);
        snippets.add(getTopNScores);
        snippets.add(getScoresWithUserId);
        snippets.add(getAverageByGameModeId);
        snippets.add(getAverageForApp);
    }

    // test Data
    int gameModeId = 0;
    int scoreId = 0;
    int appId = 961;
    int scoreCount = 10;
    int userId = 53779;


    Snippet getAverageForApp = new Snippet("get average for application") {
        @Override
        public void execute() {
            QBRatings.getAveragesByApp(new QBCallback() {
                @Override
                public void onComplete(Result result) {
                    printResultToConsole(result);
                    if (result.isSuccess()) {
                        QBAverageArrayResult averageArrayResult = (QBAverageArrayResult) result;
                        System.out.println("Average for first gameMode - " + averageArrayResult.getAverages().get(0).getValue());
                    } else {
                        handleErrors(result);
                    }
                }

                @Override
                public void onComplete(Result result, Object context) {
                }
            });
        }
    };

    Snippet getAverageByGameModeId = new Snippet("get average by game mode id") {
        @Override
        public void execute() {
            if (gameModeId != 0) {
                QBGameMode qbGameMode = new QBGameMode();
                qbGameMode.setId(gameModeId);
                QBRatings.getAverageByGameMode(qbGameMode, new QBCallback() {
                    @Override
                    public void onComplete(Result result) {
                        printResultToConsole(result);
                        if (result.isSuccess()) {
                            QBAverageResult averageResult = (QBAverageResult) result;
                            System.out.println("Average for gameMode - " + averageResult.getAverage().getValue());
                        } else {
                            handleErrors(result);
                        }
                    }

                    @Override
                    public void onComplete(Result result, Object context) {
                    }
                });
            } else {
                System.out.println("Create Game Mode before get average.");
            }
        }
    };

    Snippet getGameModes = new Snippet("get game modes") {
        @Override
        public void execute() {
            QBRatings.getGameModes(new QBCallback() {
                @Override
                public void onComplete(Result result) {
                    printResultToConsole(result);
                    if (result.isSuccess()) {
                        QBGameModeArrayResult gameModeArrayResult = (QBGameModeArrayResult) result;
                        System.out.println("GameMode count - " + gameModeArrayResult.getGameModes().size());
                    } else {
                        handleErrors(result);
                    }
                }

                @Override
                public void onComplete(Result result, Object context) {
                }
            });
        }
    };

    Snippet updateGameMode = new Snippet("update game mode") {
        @Override
        public void execute() {
            if (gameModeId != 0) {
                QBGameMode qbGameMode = new QBGameMode();
                qbGameMode.setAppId(appId);
                qbGameMode.setId(gameModeId);
                qbGameMode.setTitle("new title for game mode");
                QBRatings.updateGameMode(qbGameMode, new QBCallback() {
                    @Override
                    public void onComplete(Result result) {
                        printResultToConsole(result);
                        if (result.isSuccess()) {
                            QBGameModeResult gameModeResult = (QBGameModeResult) result;
                            System.out.println("Update gameMode title - " + gameModeResult.getGameMode().getTitle());
                        } else {
                            handleErrors(result);
                        }
                    }

                    @Override
                    public void onComplete(Result result, Object context) {
                    }
                });
            } else {
                System.out.println("Create Game Mode before updating.");
            }
        }
    };

    Snippet createGameMode = new Snippet("create game mode") {
        @Override
        public void execute() {
            QBGameMode gameMode = new QBGameMode("my game mode");
            QBRatings.createGameMode(gameMode, new QBCallbackImpl() {
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

    Snippet getGameModeWithId = new Snippet("get game mode") {
        @Override
        public void execute() {
            if (gameModeId != 0) {
                QBGameMode gameMode = new QBGameMode(gameModeId);

                QBRatings.getGameMode(gameMode, new QBCallbackImpl() {
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

    Snippet deleteGameModeWithId = new Snippet("delete game mode") {
        @Override
        public void execute() {
            if (gameModeId != 0) {
                QBGameMode gameMode = new QBGameMode(gameModeId);

                QBRatings.deleteGameMode(gameMode, new QBCallbackImpl() {
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

                QBRatings.createScore(score, new QBCallbackImpl() {
                    @Override
                    public void onComplete(Result result) {
                        printResultToConsole(result);
                        if (result.isSuccess()) {
                            scoreId = ((QBScoreResult) result).getScore().getId();
                        }
                    }
                });
            } else {
                System.out.println("Create Game Mode before create score.");
            }
        }
    };

    Snippet getScoreWithId = new Snippet("get score") {
        @Override
        public void execute() {
            if (scoreId != 0) {
                QBScore score = new QBScore(scoreId);

                QBRatings.getScore(score, new QBCallbackImpl() {
                    @Override
                    public void onComplete(Result result) {
                        printResultToConsole(result);
                        if (result.isSuccess()) {
                            QBScoreResult scoreResult = (QBScoreResult) result;
                            System.out.println("Score value - " + scoreResult.getScore().getValue());
                        } else {
                            handleErrors(result);
                        }
                    }
                });
            } else {
                System.out.println("Create Score before retrieving.");
            }
        }
    };

    Snippet deleteScoreWithId = new Snippet("delete score") {
        @Override
        public void execute() {
            if (scoreId != 0) {
                QBScore score = new QBScore(scoreId);

                QBRatings.deleteScore(score, new QBCallbackImpl() {
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

    Snippet updateScore = new Snippet("update score") {
        @Override
        public void execute() {
            if (gameModeId != 0 && scoreId != 0) {
                QBScore qbScore = new QBScore();
                qbScore.setGameModeId(gameModeId);
                qbScore.setId(scoreId);
                qbScore.setValue(123);
                QBRatings.updateScore(qbScore, new QBCallback() {
                    @Override
                    public void onComplete(Result result) {
                        printResultToConsole(result);
                        if (result.isSuccess()) {
                            QBScoreResult scoreResult = (QBScoreResult) result;
                            System.out.println("Score value - " + scoreResult.getScore().getValue());
                        } else {
                            handleErrors(result);
                        }
                    }

                    @Override
                    public void onComplete(Result result, Object context) {
                    }
                });
            } else {
                System.out.println("Create gameMode and score before updating.");
            }
        }
    };

    Snippet getTopNScores = new Snippet("get top n scores") {
        @Override
        public void execute() {
            if (gameModeId != 0) {
                QBGameMode qbGameMode = new QBGameMode();
                qbGameMode.setAppId(appId);
                qbGameMode.setId(gameModeId);
                QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder();
                requestBuilder.setCurrentPage(1);
                requestBuilder.setPerPage(20);
                QBRatings.getTopScores(qbGameMode, scoreCount, requestBuilder, new QBCallback() {
                    @Override
                    public void onComplete(Result result) {
                        printResultToConsole(result);
                        if (result.isSuccess()) {
                            QBScorePagedResult scorePagedResult = (QBScorePagedResult) result;
                            System.out.println("Value for first score - " + scorePagedResult.getScores().get(0).getValue());
                        } else {
                            handleErrors(result);
                        }
                    }

                    @Override
                    public void onComplete(Result result, Object context) {
                    }
                });
            } else {
                System.out.println("Create gameMode and several scores before get top n scores.");
            }
        }
    };

    Snippet getScoresWithUserId = new Snippet("get scores with user id") {
        @Override
        public void execute() {
            QBUser qbUser = new QBUser();
            qbUser.setId(userId);
            QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder();
            requestBuilder.setCurrentPage(1);
            requestBuilder.setPerPage(20);
            QBRatings.getScoresByUser(qbUser, requestBuilder, new QBCallback() {
                @Override
                public void onComplete(Result result) {
                    printResultToConsole(result);
                    if (result.isSuccess()) {
                        QBScorePagedResult qbScorePagedResult = (QBScorePagedResult) result;
                        System.out.println("Value for first score - " + qbScorePagedResult.getScores().get(0).getValue());
                    } else {
                        handleErrors(result);
                    }
                }

                @Override
                public void onComplete(Result result, Object context) {
                }
            });
        }
    };
}