/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dt.peng.sql.tree;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import static com.google.common.base.MoreObjects.toStringHelper;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public final class Rollup
        extends GroupingElement
{
    private final List<QualifiedName> columns;

    public Rollup(List<QualifiedName> columns)
    {
        this(Optional.empty(), columns);
    }

    public Rollup(NodeLocation location, List<QualifiedName> columns)
    {
        this(Optional.of(location), columns);
    }

    private Rollup(Optional<NodeLocation> location, List<QualifiedName> columns)
    {
        super(location);
        this.columns = ImmutableList.copyOf(requireNonNull(columns, "columns is null"));
    }

    public List<QualifiedName> getColumns()
    {
        return columns;
    }

    @Override
    public List<Set<Expression>> enumerateGroupingSets()
    {
        int numColumns = columns.size();
        return ImmutableList.<Set<Expression>>builder()
                .addAll(IntStream.range(0, numColumns)
                        .mapToObj(i -> columns.subList(0, numColumns - i)
                                .stream()
                                .map(DereferenceExpression::from)
                                .map(Expression.class::cast)
                                .collect(toSet()))
                        .collect(toList()))
                .add(ImmutableSet.of())
                .build();
    }

    @Override
    protected <R, C> R accept(AstVisitor<R, C> visitor, C context)
    {
        return visitor.visitRollup(this, context);
    }

    @Override
    public List<Node> getChildren()
    {
        return ImmutableList.of();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Rollup rollup = (Rollup) o;
        return Objects.equals(columns, rollup.columns);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(columns);
    }

    @Override
    public String toString()
    {
        return toStringHelper(this)
                .add("columns", columns)
                .toString();
    }
}
