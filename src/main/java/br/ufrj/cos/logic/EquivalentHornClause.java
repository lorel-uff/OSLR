/*
 * Online Structure Learner by Revision (OSLR) is an online relational
 * learning algorithm that can handle continuous, open-ended
 * streams of relational examples as they arrive. We employ
 * techniques from theory revision to take advantage of the already
 * acquired knowledge as a starting point, find where it should be
 * modified to cope with the new examples, and automatically update it.
 * We rely on the Hoeffding's bound statistical theory to decide if the
 * model must in fact be updated accordingly to the new examples.
 * The system is built upon ProPPR statistical relational language to
 * describe the induced models, aiming at contemplating the uncertainty
 * inherent to real data.
 *
 * Copyright (C) 2017-2018 Victor Guimarães
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

package br.ufrj.cos.logic;

import br.ufrj.cos.util.HornClauseUtils;
import br.ufrj.cos.util.LanguageUtils;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static br.ufrj.cos.util.HornClauseUtils.getSubstitutionMap;

/**
 * A container for an {@link HornClause} with improved information about it generation, in order to detect equivalent
 * clauses and make the relevant of the equivalent clauses, also relevant to this one.
 * <p>
 * Created on 12/08/17.
 *
 * @author Victor Guimarães
 */
public class EquivalentHornClause {

    protected final Atom head;
    protected final Set<Literal> clauseBody;
    protected final List<Map<Term, Term>> substitutionMaps;
    protected final Set<Term> fixedTerms;
    protected final int nextCandidateIndex;
    protected Literal lastLiteral;
    protected List<Literal> currentCandidates;
    protected List<Map<Term, Term>> currentSubstitutionMaps;

    /**
     * Constructor for a initial empty version.
     *
     * @param head the head of the clause.
     */
    public EquivalentHornClause(Atom head) {
        this.head = head;
        this.clauseBody = new LinkedHashSet<>();

        this.substitutionMaps = new ArrayList<>();
        this.substitutionMaps.add(new HashMap<>());

        this.fixedTerms = new HashSet<>(head.getTerms());

        this.nextCandidateIndex = 0;
    }

    /**
     * Constructor for a clause generated by the {@link  #buildInitialClauseCandidates(List, Map, Map)} method.
     *
     * @param head               the head
     * @param clauseBody         the body
     * @param lastLiteral        the last literal from the body
     * @param substitutionMap    the substitution map that originates the clause
     * @param nextCandidateIndex the next candidate index to prevent equivalent clauses by reordering the body
     */
    protected EquivalentHornClause(Atom head, Set<Literal> clauseBody, Literal lastLiteral,
                                   Map<Term, Term> substitutionMap, int nextCandidateIndex) {
        this.head = head;
        this.clauseBody = clauseBody;
        this.lastLiteral = lastLiteral;

        this.substitutionMaps = new ArrayList<>();
        this.substitutionMaps.add(substitutionMap);

        this.fixedTerms = this.clauseBody.stream().flatMap(l -> l.getTerms().stream()).collect(Collectors.toSet());
        this.fixedTerms.addAll(head.getTerms());

        this.nextCandidateIndex = nextCandidateIndex;
    }

    /**
     * Gets the {@link HornClause}.
     *
     * @return the {@link HornClause}
     */
    public HornClause getHornClause() {
        return new HornClause(head, new Conjunction(clauseBody));
    }

    /**
     * Creates a list of {@link EquivalentHornClause} containing a {@link EquivalentHornClause} for each substitution
     * of each candidate, skipping equivalent clauses.
     * <p>
     * It skips equivalents clauses, by checking if the free variables at the candidate atom can be renamed
     * to match the free variables of a previously selected one. If a equivalent atom {@code A} is detected, the
     * substitution map that makes it equals to another a previous atom {@code B} is stored along with {@code B}. In
     * this case, when a rule from a set of candidates is selected for further refinements, it stores a substitution map
     * that, if applied to the candidates, makes the relevants atoms of discarded equivalent atoms, also relevant to
     * the selected rule.
     *
     * @param candidates list of candidates
     * @param skipAtom   map to save the previous atom equivalent to the currents
     * @param skipClause map to save the previous clauses equivalent to the currents
     * @return a list of {@link EquivalentHornClause} containing a {@link EquivalentHornClause} for each substitution
     * of each candidate, skipping equivalent clauses.
     */
    public List<EquivalentHornClause> buildInitialClauseCandidates(final List<? extends Literal> candidates,
                                                                   Map<EquivalentClauseAtom, EquivalentClauseAtom>
                                                                           skipAtom,
                                                                   Map<EquivalentClauseAtom, EquivalentHornClause>
                                                                           skipClause) {
        if (nextCandidateIndex >= candidates.size()) { return Collections.emptyList(); }
        List<EquivalentHornClause> hornClauses = new ArrayList<>(candidates.size() - nextCandidateIndex);

        for (int i = nextCandidateIndex; i < candidates.size(); i++) {
            if (candidates.get(i).isNegated()) { continue; }
            findCandidatesBySubstitutions(candidates.get(i), l -> true);
            processingSubstitutedCandidates(skipAtom, skipClause, hornClauses, i);
        }
        return hornClauses;
    }

    /**
     * Finds the candidates by applying the possible substitution maps. The candidates are stored in the
     * {@link #currentCandidates} and the used maps at {@link #currentSubstitutionMaps}, keeping the respective indexes.
     *
     * @param candidate the candidate
     * @param filter    add only the literals that is tested as true by the filter
     */
    protected void findCandidatesBySubstitutions(Literal candidate, Predicate<? super Literal> filter) {
        currentCandidates = new ArrayList<>(substitutionMaps.size());
        currentSubstitutionMaps = new ArrayList<>(substitutionMaps.size());
        Literal literal;
        //noinspection IfStatementWithNegatedCondition
        if (!Collections.disjoint(fixedTerms, candidate.getTerms())) {
            literal = LanguageUtils.applySubstitution(candidate, substitutionMaps.get(0));
            testAndAdd(filter, literal, substitutionMaps.get(0));
        } else {
            for (Map<Term, Term> substitutionMap : substitutionMaps) {
                for (Map.Entry<Term, Term> entry : substitutionMap.entrySet()) {
                    if (candidate.getTerms().contains(entry.getKey()) && fixedTerms.contains(entry.getValue())) {
                        literal = LanguageUtils.applySubstitution(candidate, substitutionMap);
                        testAndAdd(filter, literal, substitutionMap);
                    }
                }
            }
        }
    }

    /**
     * Process the substituted candidates.
     *
     * @param skipAtom       map to save the previous atom equivalent to the currents
     * @param skipClause     map to save the previous clauses equivalent to the currents
     * @param hornClauses    the horn clauses list to append
     * @param candidateIndex the initial index of the candidates
     */
    @SuppressWarnings("OverlyLongMethod")
    protected void processingSubstitutedCandidates(Map<EquivalentClauseAtom, EquivalentClauseAtom> skipAtom,
                                                   Map<EquivalentClauseAtom, EquivalentHornClause> skipClause,
                                                   List<EquivalentHornClause> hornClauses, int candidateIndex) {
        EquivalentClauseAtom currentAtom;     // the current atom
        EquivalentClauseAtom equivalentAtom;  // the previous selected atom equivalent to the current one

        Map<Term, Term> substitutionMap; // the current substitution map, so the new literal relevant to the an atom ...
        // equivalent to a previously selected atom can be made relevant to the previous too
        Map<Term, Term> subMap; // auxiliary variable to the substitution map

        Set<Literal> currentSet;        // the current rule body with the additional literal

        Literal candidate;
        EquivalentHornClause equivalentHornClause;
        for (int j = 0; j < currentCandidates.size(); j++) {
            candidate = currentCandidates.get(j);
            if (clauseBody.contains(candidate)) { continue; }
            currentAtom = new EquivalentClauseAtom(clauseBody, candidate, fixedTerms);
            equivalentAtom = skipAtom.get(currentAtom);
            if (equivalentAtom == null) {
                substitutionMap = new HashMap<>(currentSubstitutionMaps.get(j));

                currentSet = new LinkedHashSet<>(clauseBody);
                currentSet.add(candidate);

                skipAtom.put(currentAtom, currentAtom);
                equivalentHornClause = new EquivalentHornClause(head, currentSet, candidate, substitutionMap,
                                                                candidateIndex + 1);
                skipClause.put(currentAtom, equivalentHornClause);

                hornClauses.add(equivalentHornClause);
            } else {
                equivalentHornClause = skipClause.get(equivalentAtom);
                substitutionMap = new HashMap<>(equivalentHornClause.substitutionMaps.get(0));
                subMap = getSubstitutionMap(currentAtom, equivalentAtom, fixedTerms);
                if (subMap != null) { substitutionMap.putAll(subMap); }
                equivalentHornClause.substitutionMaps.add(substitutionMap);
            }
        }
    }

    private void testAndAdd(Predicate<? super Literal> filter, Literal literal, Map<Term, Term> substitutionMap) {
        if (filter.test(literal)) {
            currentCandidates.add(literal);
            currentSubstitutionMaps.add(substitutionMap);
        }
    }

    /**
     * Creates the new candidate rules, by adding on possible literal to the current rule's body. The current rule
     * is represented by the head and body parameters.
     * <p>
     * In addition, it skips equivalents sets, by checking if the free variables at the candidate atom can be renamed
     * to match the free variables of a previously selected one. If a equivalent atom {@code A} is detected, the
     * substitution map that makes it equals to another a previous atom {@code B} is stored along with {@code B}. In
     * this case, when a rule from a set of candidates is selected for further refinements, it stores a substitution map
     * that, if applied to the candidates, makes the relevants atoms of discarded equivalent atoms, also relevant to
     * the selected rule.
     *
     * @param candidates the {@link Iterable} of candidates
     * @return the set of candidate clauses
     */
    public List<EquivalentHornClause> buildAppendCandidatesFromClause(final Collection<? extends Literal> candidates) {
        if (candidates == null || candidates.isEmpty()) { return Collections.emptyList(); }
        List<EquivalentHornClause> hornClauses = new ArrayList<>();

        final Map<EquivalentClauseAtom, EquivalentHornClause> skipClause = new HashMap<>();
        final Map<EquivalentClauseAtom, EquivalentClauseAtom> skipAtom = new HashMap<>();
        for (Literal candidate : candidates) {
            findCandidatesBySubstitutions(candidate, l -> HornClauseUtils.willRuleBeSafe(head, clauseBody, l));
            processingSubstitutedCandidates(skipAtom, skipClause, hornClauses, -1);
        }
        return hornClauses;
    }

    /**
     * Gets the head.
     *
     * @return the head
     */
    public Atom getHead() {
        return head;
    }

    /**
     * Gets the body.
     *
     * @return the body
     */
    public Set<Literal> getClauseBody() {
        return clauseBody;
    }

    /**
     * Gets the last literal from the clause body.
     *
     * @return the last literal from the clause body
     */
    public Literal getLastLiteral() {
        return lastLiteral;
    }

    /**
     * Gets the substitution maps.
     *
     * @return the substitution maps
     */
    public List<Map<Term, Term>> getSubstitutionMaps() {
        return substitutionMaps;
    }

    @Override
    public int hashCode() {
        int result = head.hashCode();
        result = 31 * result + clauseBody.hashCode();
//        result = 31 * result + substitutionMaps.hashCode();
//        result = 31 * result + nextCandidateIndex;
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (!(o instanceof EquivalentHornClause)) { return false; }

        EquivalentHornClause that = (EquivalentHornClause) o;

//        if (nextCandidateIndex != that.nextCandidateIndex) { return false; }
        if (!head.equals(that.head)) { return false; }
        if (!clauseBody.equals(that.clauseBody)) { return false; }
        return substitutionMaps.equals(that.substitutionMaps);
    }

    @Override
    public String toString() {
        return LanguageUtils.formatHornClause(head, clauseBody);
    }

}
