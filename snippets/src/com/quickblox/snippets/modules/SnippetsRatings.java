package com.quickblox.snippets.modules;

import android.content.Context;
import android.util.Log;

import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.internal.core.request.QBPagedRequestBuilder;
import com.quickblox.module.ratings.QBRatings;
import com.quickblox.module.ratings.model.QBAverage;
import com.quickblox.module.ratings.model.QBGameMode;
import com.quickblox.module.ratings.model.QBScore;
import com.quickblox.module.ratings.result.QBAverageArrayResult;
import com.quickblox.module.ratings.result.QBAverageResult;
import com.quickblox.module.ratings.result.QBGameModeArrayResult;
import com.quickblox.module.ratings.result.QBGameModeResult;
import com.quickblox.module.ratings.result.QBScorePagedResult;
import com.quickblox.module.ratings.result.QBScoreResult;
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

    private static final String TAG = SnippetsRatings.class.getSimpleName();

    public SnippetsRatings(Context context) {
        super(context);

        snippets.add(createGameMode);
        snippets.add(getGameModeWithId);
        snippets.add(updateGameMode);
        snippets.add(getGameModes);
        snippets.add(deleteGameModeWithId);

        snippets.add(createScore);
        snippets.add(getScoreWithId);
        snippets.add(updateScore);
        snippets.add(deleteScoreWithId);
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
            QBGameMode gameMode = new QBGameMode("Guitar hero mode");

            QBRatings.createGameMode(gameMode, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBGameModeResult gameModeResult = (QBGameModeResult) result;

                        Log.i(TAG, ">>> new game mode is:" + gameModeResult.getGameMode().toString());
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
            QBGameMode gameMode = new QBGameMode(311);
            QBRatings.getGameMode(gameMode, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBGameModeResult gameModeResult = (QBGameModeResult) result;
                        Log.i(TAG, ">>>game mode:" + gameModeResult.getGameMode().toString());
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
            qbGameMode.setId(310);
            qbGameMode.setTitle("new title for game mode yeahhh");

            QBRatings.updateGameMode(qbGameMode, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBGameModeResult gameModeResult = (QBGameModeResult) result;
                        Log.i(TAG, "GameMode " + gameModeResult.getGameMode().toString());
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
            QBGameMode gameMode = new QBGameMode(310);

            QBRatings.deleteGameMode(gameMode, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        Log.i(TAG, ">>>game mode successfully deleted:");
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
                        Log.i(TAG, "GameMode list - " + gameModeArrayResult.getGameModes().toString());
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
            score.setGameModeId(311);
            score.setValue(4);

            QBRatings.createScore(score, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBScoreResult qbScoreResult = (QBScoreResult) result;
                        Log.i(TAG, ">>>game mode successfully deleted:" + qbScoreResult.getScore().toString());
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
            QBScore score = new QBScore(1945);
            Date date = new Date(System.currentTimeMillis());
            score.setCreatedAt(date);

            QBRatings.getScore(score, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBScoreResult scoreResult = (QBScoreResult) result;
                        Log.i(TAG, "Score " + scoreResult.getScore().toString());
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
            QBScore score = new QBScore(1945);

            QBRatings.deleteScore(score, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        Log.i(TAG, "Score deleted");
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
            qbScore.setId(1945);
            qbScore.setValue(1945);

            QBRatings.updateScore(qbScore, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBScoreResult scoreResult = (QBScoreResult) result;


                        Log.i(TAG, "Score - " + scoreResult.getScore().toString());
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
            qbGameMode.setId(311);

            QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder();
            requestBuilder.setPage(1);
            requestBuilder.setPerPage(20);

            QBRatings.getTopScores(qbGameMode, 10, requestBuilder, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {

                    if (result.isSuccess()) {
                        QBScorePagedResult scorePagedResult = (QBScorePagedResult) result;
                        Log.i(TAG, "Score list " + scorePagedResult.getScores().toString());
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
            qbUser.setId(53779);

            QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder();
            requestBuilder.setPage(1);
            requestBuilder.setPerPage(20);

            QBRatings.getScoresByUser(qbUser, requestBuilder, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBScorePagedResult qbScorePagedResult = (QBScorePagedResult) result;
                        Log.i(TAG, "Score list - " + qbScorePagedResult.getScores().toString());
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
                        Log.i(TAG, "AverageList- " + averageList.toString());
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
            qbGameMode.setId(311);
            QBRatings.getAverageByGameMode(qbGameMode, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBAverageResult averageResult = (QBAverageResult) result;
                        Log.i(TAG, "Average - " + averageResult.getAverage().toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };
}