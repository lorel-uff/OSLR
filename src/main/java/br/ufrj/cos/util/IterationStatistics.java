/*
 * Probabilist Logic Learner is a system to learn probabilistic logic
 * programs from data and use its learned programs to make inference
 * and answer queries.
 *
 * Copyright (C) 2017 Victor Guimarães
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package br.ufrj.cos.util;

import br.ufrj.cos.knowledge.theory.evaluation.metric.TheoryMetric;

import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Class to hold the statistics of an iteration experiment. The idea is to serialize this class using the Yaml
 * library in order to be able to consult the statistics of a run in a way that is both human and machine friendly.
 * <p>
 * Created on 02/08/17.
 *
 * @author Victor Guimarães
 */
@SuppressWarnings("HardCodedStringLiteral")
public class IterationStatistics {

    /**
     * The number format.
     */
    public static final NumberFormat NUMBER_FORMAT = NumberFormat.getIntegerInstance();

    protected int numberOfIterations;
    protected String iterationPrefix;
    protected String targetRelation;

    protected List<Integer> iterationKnowledgeSizes;
    protected List<Integer> iterationExamplesSizes;

    protected List<Map<TheoryMetric, Double>> iterationTrainEvaluation;
    protected List<Map<TheoryMetric, Double>> iterationTestEvaluation;

    protected long begin;
    protected long beginTraining;
    protected long endTime;

    /**
     * Default constructor to allow YAML serialization.
     */
    public IterationStatistics() {
    }

    /**
     * Constructor with the number of iterations.
     *
     * @param numberOfIterations the number of iterations
     */
    public IterationStatistics(int numberOfIterations) {
        this.numberOfIterations = numberOfIterations;
        iterationKnowledgeSizes = new ArrayList<>(numberOfIterations);
        iterationExamplesSizes = new ArrayList<>(numberOfIterations);

        iterationTrainEvaluation = new ArrayList<>(numberOfIterations);
        iterationTestEvaluation = new ArrayList<>(numberOfIterations);
    }

    /**
     * Gets the number of iterations.
     *
     * @return the number of iterations
     */
    public int getNumberOfIterations() {
        return numberOfIterations;
    }

    /**
     * Sets the number of iterations.
     *
     * @param numberOfIterations the number of iterations
     */
    public void setNumberOfIterations(int numberOfIterations) {
        this.numberOfIterations = numberOfIterations;
    }

    /**
     * Gets the iteration prefix.
     *
     * @return the iteration prefix
     */
    public String getIterationPrefix() {
        return iterationPrefix;
    }

    /**
     * Sets the iteration prefix.
     *
     * @param iterationPrefix the iteration prefix
     */
    public void setIterationPrefix(String iterationPrefix) {
        this.iterationPrefix = iterationPrefix;
    }

    /**
     * Gets the target relation.
     *
     * @return the target relation
     */
    public String getTargetRelation() {
        return targetRelation;
    }

    /**
     * Sets the target relation.
     *
     * @param targetRelation the target relation
     */
    public void setTargetRelation(String targetRelation) {
        this.targetRelation = targetRelation;
    }

    /**
     * Gets the iterations knowledge bases' sizes.
     *
     * @return the iterations knowledge bases' sizes
     */
    public List<Integer> getIterationKnowledgeSizes() {
        return iterationKnowledgeSizes;
    }

    /**
     * Sets the iterations knowledge bases' sizes.
     *
     * @param iterationKnowledgeSizes the iterations knowledge bases' sizes
     */
    public void setIterationKnowledgeSizes(List<Integer> iterationKnowledgeSizes) {
        this.iterationKnowledgeSizes = iterationKnowledgeSizes;
    }

    /**
     * Gets the iterations examples' sizes.
     *
     * @return the iterations examples' sizes
     */
    public List<Integer> getIterationExamplesSizes() {
        return iterationExamplesSizes;
    }

    /**
     * Sets the iterations examples' sizes.
     *
     * @param iterationExamplesSizes the iterations examples' sizes
     */
    public void setIterationExamplesSizes(List<Integer> iterationExamplesSizes) {
        this.iterationExamplesSizes = iterationExamplesSizes;
    }

    /**
     * Gets the iterations train evaluations.
     *
     * @return the iterations train evaluations
     */
    public List<Map<TheoryMetric, Double>> getIterationTrainEvaluation() {
        return iterationTrainEvaluation;
    }

    /**
     * Sets the iterations train evaluations.
     *
     * @param iterationTrainEvaluation the iterations train evaluations
     */
    public void setIterationTrainEvaluation(
            List<Map<TheoryMetric, Double>> iterationTrainEvaluation) {
        this.iterationTrainEvaluation = iterationTrainEvaluation;
    }

    /**
     * Gets the iterations test evaluations.
     *
     * @return the iterations test evaluations
     */
    public List<Map<TheoryMetric, Double>> getIterationTestEvaluation() {
        return iterationTestEvaluation;
    }

    /**
     * Sets the iterations test evaluations.
     *
     * @param iterationTestEvaluation the iterations test evaluations
     */
    public void setIterationTestEvaluation(
            List<Map<TheoryMetric, Double>> iterationTestEvaluation) {
        this.iterationTestEvaluation = iterationTestEvaluation;
    }

    /**
     * Gets the begin time.
     *
     * @return the begin time
     */
    public long getBegin() {
        return begin;
    }

    /**
     * Sets the begin time.
     *
     * @param begin the begin time
     */
    public void setBegin(long begin) {
        this.begin = begin;
    }

    /**
     * Gets the begin training time.
     *
     * @return the begin training time
     */
    public long getBeginTraining() {
        return beginTraining;
    }

    /**
     * Sets the begin training time.
     *
     * @param beginTraining the begin training time
     */
    public void setBeginTraining(long beginTraining) {
        this.beginTraining = beginTraining;
    }

    /**
     * Gets the ending time.
     *
     * @return the ending time
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * Sets the ending time.
     *
     * @param endTime the ending time
     */
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        final List<TheoryMetric> sortedMetrics = getSortedMetrics();

        StringBuilder description = new StringBuilder();
        appendGeneralInformation(description);

        description.append("\t").append("Info by Iteration:\n");
        int totalKnowledgeSize = 0;
        int totalExampleSize = 0;
        for (int i = 0; i < numberOfIterations; i++) {
            totalKnowledgeSize += iterationKnowledgeSizes.get(i);
            totalExampleSize += iterationExamplesSizes.get(i);
            description.append("\t\tIteration:\t").append(i).append("\n");

            appendKnowledgeSize(description, totalKnowledgeSize, i);

            appendExampleSize(description, totalExampleSize, i);

            appendEvaluation(sortedMetrics, description, i, iterationTrainEvaluation, "Train");
            appendEvaluation(sortedMetrics, description, i, iterationTestEvaluation, "Test");

            description.append("\n");
        }

        appendRunTime(description);

        return description.toString();
    }

    /**
     * Gets the sorted metrics from the evaluation lists.
     *
     * @return the sorted metrics.
     */
    protected List<TheoryMetric> getSortedMetrics() {
        final Set<TheoryMetric> metricSet = new HashSet<>();
        iterationTrainEvaluation.stream().flatMap(m -> m.keySet().stream()).forEach(metricSet::add);
        iterationTestEvaluation.stream().flatMap(m -> m.keySet().stream()).forEach(metricSet::add);
        return metricSet.stream().sorted(Comparator.comparing(o -> o.getClass().getSimpleName()))
                .collect(Collectors.toList());
    }

    /**
     * Appends the general run information to the description.
     *
     * @param description the description
     */
    protected void appendGeneralInformation(StringBuilder description) {
        description.append(this.getClass().getSimpleName()).append("\n");

        description.append("\t").append("Number of Iterations:\t").append(numberOfIterations).append("\n");
        description.append("\t").append("Iterations Prefix:\t").append(iterationPrefix).append("\n");
        description.append("\t").append("Target Relation:\t\t").append(targetRelation).append("\n");
    }

    /**
     * Appends the knowledge size to the description
     *
     * @param description        the description
     * @param totalKnowledgeSize the total size until the index
     * @param index              the index of the iteration
     */
    protected void appendKnowledgeSize(StringBuilder description, int totalKnowledgeSize, int index) {
        description.append("\t\t\t- New Knowledge of Iteration:\t");
        description.append(NUMBER_FORMAT.format(iterationKnowledgeSizes.get(index))).append("\n");
        description.append("\t\t\t- Iteration Knowledge Size:\t");
        description.append(NUMBER_FORMAT.format(totalKnowledgeSize)).append("\n");
    }

    /**
     * Appends the example size to the description
     *
     * @param description      the description
     * @param totalExampleSize the total size until the index
     * @param index            the index of the iteration
     */
    protected void appendExampleSize(StringBuilder description, int totalExampleSize, int index) {
        description.append("\t\t\t- New Example of Iteration:\t");
        description.append(NUMBER_FORMAT.format(iterationExamplesSizes.get(index))).append("\n");
        description.append("\t\t\t- Iteration Example Size:\t");
        description.append(NUMBER_FORMAT.format(totalExampleSize)).append("\n");
    }

    /**
     * Appends the evaluation of the iteration to the description.
     *
     * @param sortedMetrics       the metrics
     * @param description         the description
     * @param index               the index of the iteration
     * @param iterationEvaluation the iteration evaluation
     * @param label               the label of the evaluation type {@code {Train, Test}}
     */
    @SuppressWarnings("MethodWithTooManyParameters")
    protected void appendEvaluation(List<TheoryMetric> sortedMetrics, StringBuilder description, int index,
                                    List<Map<TheoryMetric, Double>> iterationEvaluation, String label) {
        Double evaluation;
        if (index < iterationEvaluation.size()) {
            description.append("\t\t\t- ").append(label).append(" Evaluation:\n");
            for (TheoryMetric metric : sortedMetrics) {
                evaluation = iterationTrainEvaluation.get(index).get(metric);
                if (evaluation != null) {
                    description.append("\t\t\t\t- ").append(metric).append(":\t").append(evaluation);
                }
            }
        }
    }

    /**
     * Appends the run time to the description.
     *
     * @param description the description
     */
    protected void appendRunTime(StringBuilder description) {
        description.append("\t").append("Run time:\n");
        description.append("\t\t").append("- IO time:\t\t\t")
                .append(TimeUtils.formatNanoDifference(begin, beginTraining));
        description.append("\t\t").append("- Training time:\t")
                .append(TimeUtils.formatNanoDifference(beginTraining, endTime));
        description.append("\t\t").append("- Total time:\t")
                .append(TimeUtils.formatNanoDifference(begin, endTime));
    }

    /**
     * Adds a element to the {@link #iterationKnowledgeSizes}.
     *
     * @param element the element
     */
    public void addIterationKnowledgeSizes(Integer element) {
        iterationKnowledgeSizes.add(element);
    }

    /**
     * Adds a element to the {@link #iterationExamplesSizes}.
     *
     * @param element the element
     */
    public void addIterationExamplesSizes(Integer element) {
        iterationExamplesSizes.add(element);
    }

    /**
     * Adds a element to the {@link #iterationTrainEvaluation}.
     *
     * @param element the element
     */
    public void addIterationTrainEvaluation(Map<TheoryMetric, Double> element) {
        iterationTrainEvaluation.add(element);
    }

    /**
     * Adds a element to the {@link #iterationTestEvaluation}.
     *
     * @param element the element
     */
    public void addIterationTestEvaluation(Map<TheoryMetric, Double> element) {
        iterationTestEvaluation.add(element);
    }

}
