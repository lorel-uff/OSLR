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

package br.ufrj.cos.engine.proppr.query.answerer;

import br.ufrj.cos.engine.proppr.ProPprEngineSystemTranslator;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.util.IterableConverter;
import edu.cmu.ml.proppr.prove.wam.Query;

import java.util.Iterator;

/**
 * An {@link Iterator} that transforms {@link Example}s from the system representation to {@link Query}is
 * in ProPPR's representation.
 * <p>
 * Created on 05/05/17.
 *
 * @author Victor Guimarães
 */
public class QueryIterable extends IterableConverter<Example, Query> {

    /**
     * Constructs from an {@link Iterable}.
     *
     * @param iterable the {@link Iterable}
     */
    public QueryIterable(Iterable<? extends Example> iterable) {
        super(iterable);
    }

    /**
     * Constructs from an {@link Iterator}.
     *
     * @param iterator the {@link Iterator}
     */
    public QueryIterable(Iterator<? extends Example> iterator) {
        super(iterator);
    }

    /**
     * Constructs from an array.
     *
     * @param iterator the array
     */
    public QueryIterable(Example... iterator) {
        super(iterator);
    }

    @Override
    public Query processInToOut(Example example) {
        return ProPprEngineSystemTranslator.atomToQuery(example.getGoalQuery());
    }

}
