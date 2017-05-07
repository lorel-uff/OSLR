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

package br.ufrj.cos.knowledge.example;

import br.ufrj.cos.logic.Atom;
import br.ufrj.cos.logic.Term;
import br.ufrj.cos.logic.Variable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a training iterator. This training iterator might have more than one positive iterator (e.g. in the
 * case it is in the ProPPR form)
 * <p>
 * Created on 26/04/17.
 *
 * @author Victor Guimarães
 */
public interface Example {

    /**
     * Gets the {@link Atom} representation of the example. In the case of the ProPPR example, get the goal
     *
     * @return the {@link Atom} representation of the example
     */
    public Atom getAtom();

    /**
     * Gets the {@link Term}s of the positive iterator.
     *
     * @return the {@link Term}
     */
    public Collection<Term> getPositiveTerms();

    /**
     * Checks if the example is positive. In the case of the ProPPR example, check if it at least has a positive part.
     *
     * @return {@code true} if the example is positive, {@code false} otherwise
     */
    public abstract boolean isPositive();

    /**
     * Gets a {@link Map} to map the {@link Term}s in the example into variables. Specially useful when mapping a set
     * of {@link Term}s in the same variable, as the ProPPR example semantics does.
     *
     * @return the {@link Map}
     */
    public default Map<Term, Variable> getVariableMap() {
        return new HashMap<>();
    }

    /**
     * Gets the goal query from an example.
     *
     * @return the goal query
     */
    public Atom getGoalQuery();

    /**
     * Gets the grounded queries from a example, i.e. the grounds for the goal query.
     *
     * @return the grounded queries
     */
    public Iterable<? extends AtomExample> getGroundedQuery();

    @Override
    public int hashCode();

    @Override
    public boolean equals(Object o);

    @Override
    public String toString();

}
