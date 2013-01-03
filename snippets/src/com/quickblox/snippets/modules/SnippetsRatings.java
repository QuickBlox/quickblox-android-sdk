package com.quickblox.snippets.modules;

import android.content.Context;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.internal.core.request.QBPagedRequestBuilder;
import com.quickblox.module.ratings.QBRatings;
import com.quickblox.module.ratings.model.QBAverage;
import com.quickblox.module.ratings.model.QBGameMode;
import com.quickblox.module.ratings.model.QBScore;
import com.quickblox.module.ratings.result.*;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.snippets.Snippet;
import com.quickblox.snippets.Snippets;

import java.util.Date;
import java.util.List;

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

    //
    ///////////////////////////////////////////// Game mode /////////////////////////////////////////////
    //
    Snippet createGameMode = new Snippet("create game mode") {
        @Override
        public void execute() {
            QBGameMode gameMode = new QBGameMode("my game mode");

            QBRatings.createGameMode(gameMode, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBGameModeResult gameModeResult = (QBGameModeResult) result;

                        System.out.println(">>> new game mode is:" + gameModeResult.getGameMode().toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet getGameModeWithId = new Snippet("get game mode") {
        @Override
        public void execute() {
            QBGameMode gameMode = new QBGameMode(412);
            QBRatings.getGameMode(gameMode, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBGameModeResult gameModeResult = (QBGameModeResult) result;
                        System.out.println(">>>game mode:" + gameModeResult.getGameMode().toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet updateGameMode = new Snippet("update game mode") {
        @Override
        public void execute() {
            QBGameMode qbGameMode = new QBGameMode();
            qbGameMode.setId(308);
            qbGameMode.setTitle("new title for game mode");

            QBRatings.updateGameMode(qbGameMode, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBGameModeResult gameModeResult = (QBGameModeResult) result;
                        System.out.println("GameMode " + gameModeResult.getGameMode().toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet deleteGameModeWithId = new Snippet("delete game mode") {
        @Override
        public void execute() {
            QBGameMode gameMode = new QBGameMode(213);

            QBRatings.deleteGameMode(gameMode, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        System.out.println(">>>game mode successfully deleted:");
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet getGameModes = new Snippet("get game modes") {
        @Override
        public void execute() {
            QBRatings.getGameModes(new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {

                    if (result.isSuccess()) {
                        QBGameModeArrayResult gameModeArrayResult = (QBGameModeArrayResult) result;
                        System.out.println("GameMode list - " + gameModeArrayResult.getGameModes().toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    //
    ///////////////////////////////////////////// Scores /////////////////////////////////////////////
    //
    Snippet createScore = new Snippet("create score") {
        @Override
        public void execute() {
            QBScore score = new QBScore();
            score.setGameModeId(543);
            score.setValue(100500);

            QBRatings.createScore(score, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBScoreResult qbScoreResult = (QBScoreResult) result;
                        System.out.println(">>>game mode successfully deleted:" + qbScoreResult.getScore().toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet getScoreWithId = new Snippet("get score") {
        @Override
        public void execute() {
            QBScore score = new QBScore(123);
            Date date = new Date(System.currentTimeMillis());
            score.setCreatedAt(date);

            QBRatings.getScore(score, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBScoreResult scoreResult = (QBScoreResult) result;
                        System.out.println("Score " + scoreResult.getScore().toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet deleteScoreWithId = new Snippet("delete score") {
        @Override
        public void execute() {
            QBScore score = new QBScore(23);

            QBRatings.deleteScore(score, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        System.out.println("Score deleted");
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet updateScore = new Snippet("update score") {
        @Override
        public void execute() {
            QBScore qbScore = new QBScore();
            qbScore.setId(1879);
            qbScore.setValue(123);

            QBRatings.updateScore(qbScore, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBScoreResult scoreResult = (QBScoreResult) result;


                        System.out.println("Score - " + scoreResult.getScore().toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet getTopNScores = new Snippet("get top n scores") {
        @Override
        public void execute() {
            QBGameMode qbGameMode = new QBGameMode();
            qbGameMode.setId(42);

            QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder();
            requestBuilder.setPage(1);
            requestBuilder.setPerPage(20);

            QBRatings.getTopScores(qbGameMode, 10, requestBuilder, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {

                    if (result.isSuccess()) {
                        QBScorePagedResult scorePagedResult = (QBScorePagedResult) result;
                        System.out.println("Score list " + scorePagedResult.getScores().toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet getScoresWithUserId = new Snippet("get scores with user id") {
        @Override
        public void execute() {
            QBUser qbUser = new QBUser();
            qbUser.setId(134);

            QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder();
            requestBuilder.setPage(1);
            requestBuilder.setPerPage(20);

            QBRatings.getScoresByUser(qbUser, requestBuilder, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBScorePagedResult qbScorePagedResult = (QBScorePagedResult) result;
                        System.out.println("Score list - " + qbScorePagedResult.getScores().toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };


    //
    ///////////////////////////////////////////// Average /////////////////////////////////////////////
    //
    Snippet getAverageForApp = new Snippet("get average for application") {
        @Override
        public void execute() {
            QBRatings.getAveragesByApp(new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {

                    if (result.isSuccess()) {
                        QBAverageArrayResult averageArrayResult = (QBAverageArrayResult) result;
                        List<QBAverage> averageList = averageArrayResult.getAverages();
                        System.out.println("AverageList- " + averageList.toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet getAverageByGameModeId = new Snippet("get average by game mode id") {
        @Override
        public void execute() {
            QBGameMode qbGameMode = new QBGameMode();
            qbGameMode.setId(34);
            QBRatings.getAverageByGameMode(qbGameMode, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBAverageResult averageResult = (QBAverageResult) result;
                        System.out.println("Average - " + averageResult.getAverage().toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };
}