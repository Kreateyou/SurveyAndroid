package com.recoveryrecord.surveyandroid.condition;

import android.util.Log;

import com.recoveryrecord.surveyandroid.Answer;
import com.recoveryrecord.surveyandroid.AnswerProvider;

import java.util.HashMap;
import java.util.Map;

public class ConditionEvaluator {
    private static final String TAG = ConditionEvaluator.class.getSimpleName();

    private AnswerProvider mAnswerProvider;
    private CustomConditionHandler mCustomConditionHandler;

    public ConditionEvaluator(AnswerProvider answerProvider) {
        mAnswerProvider = answerProvider;
    }

    public void setCustomConditionHandler(CustomConditionHandler handler) {
        mCustomConditionHandler = handler;
    }

    public boolean isConditionMet(Condition condition) {
        boolean result = false;
        if (condition == null) {
            result =  true;
        } else if (condition instanceof SimpleCondition) {
            result = isConditionMet((SimpleCondition) condition);
        } else if (condition instanceof DecisionCondition) {
            result = isConditionMet((DecisionCondition) condition);
        } else if (condition instanceof CustomCondition) {
            result = isConditionMet((CustomCondition) condition);
        }
        Log.d(TAG, "Evaluated Condition: " + condition + " as " + result);
        return result;
    }

    private boolean isConditionMet(SimpleCondition simpleCondition) {
        Answer answer = mAnswerProvider.answerFor(simpleCondition.id);
        if (simpleCondition.subid != null && answer != null) {
            answer = answer.getValueMap() == null ? null : answer.getValueMap().get(simpleCondition.subid);
        }
        // Empty answer reasults depend on the operation
        if (answer == null) {
            switch (simpleCondition.operation) {
                case "equals":
                    return simpleCondition.value.equals(null);
                case "not equals":
                    return !simpleCondition.value.equals(null);
                case "greater than":
                case "greater than or equal to":
                case "less than":
                case "less than or equal to":
                    return false;
                case "contains":
                    return false;
                case "not contains":
                    return true;
            }
        }
        switch (simpleCondition.operation) {
            case "equals":
                return simpleCondition.value.equals(answer.getValue());
            case "not equals":
                return !simpleCondition.value.equals(answer.getValue());
            case "greater than":
                Double doubleValue = Double.parseDouble(simpleCondition.value);
                Double doubleAnswer = Double.parseDouble(answer.getValue());
                return doubleAnswer.compareTo(doubleValue) > 0;
            case "greater than or equal to":
                doubleValue = Double.parseDouble(simpleCondition.value);
                doubleAnswer = Double.parseDouble(answer.getValue());
                return doubleAnswer.compareTo(doubleValue) >= 0;
            case "less than":
                doubleValue = Double.parseDouble(simpleCondition.value);
                doubleAnswer = Double.parseDouble(answer.getValue());
                return doubleAnswer.compareTo(doubleValue) < 0;
            case "less than or equal to":
                doubleValue = Double.parseDouble(simpleCondition.value);
                doubleAnswer = Double.parseDouble(answer.getValue());
                return doubleAnswer.compareTo(doubleValue) <= 0;
            case "contains":
                return answer.getValueList() != null && answer.getValueList().contains(simpleCondition.value);
            case "not contains":
                return answer.getValueList() != null && !answer.getValueList().contains(simpleCondition.value);
        }
        return false;
    }

    private boolean isConditionMet(DecisionCondition decisionCondition) {
        for (Condition condition : decisionCondition.subconditions) {
            if (decisionCondition.operation.equals("or") && isConditionMet(condition)) {
                return true;
            } else if (decisionCondition.operation.equals("and") && !isConditionMet(condition)) {
                return false;
            }
        }
        return decisionCondition.operation.equals("and");
    }

    private boolean isConditionMet(CustomCondition customCondition) {
        if (mCustomConditionHandler == null) {
            throw new IllegalStateException("CustomConditionHandler must be set!");
        }
        Map<String, Answer> answerMap = new HashMap<>();
        for (String questionId : customCondition.ids) {
            answerMap.put(questionId, mAnswerProvider.answerFor(questionId));
        }
        return mCustomConditionHandler.isConditionMet(answerMap, customCondition.extra);
    }
}
