package com.recoveryrecord.surveyandroid;

import java.util.HashMap;
import java.util.Map;

public class QuestionState {
    private static final String ANSWER_KEY = "answer";
    private static final String QUESTION_ID_KEY = "question_id";

    private Map<String, String> mQuestionData;
    private OnQuestionStateChangedListener mListener;

    QuestionState(String questionId, OnQuestionStateChangedListener listener) {
        mQuestionData = new HashMap<>();
        mQuestionData.put(QUESTION_ID_KEY, questionId);
        mListener = listener;
    }

    public void put(String key, String value) {
        if (key.equals(QUESTION_ID_KEY)) {
            throw new IllegalArgumentException("The QuestionId cannot be updated!");
        } else if (key.equals(ANSWER_KEY)) {
            throw new IllegalArgumentException("The answer should be updated via setAnswer!");
        }
        mQuestionData.put(key, value);
        if (mListener != null) {
            mListener.questionStateChanged(this);
        }
    }

    public void setAnswer(String answer) {
        mQuestionData.put(ANSWER_KEY, answer);
        if (mListener != null) {
            mListener.questionAnswered(this);
        }
    }

    public String answer() {
        return mQuestionData.get(ANSWER_KEY);
    }

    public boolean isAnswered() {
        return mQuestionData.containsKey(ANSWER_KEY);
    }

    public String id() {
        return mQuestionData.get(QUESTION_ID_KEY);
    }
}