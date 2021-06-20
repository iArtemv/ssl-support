/*******************************************************************************
 * Copyright (C) 2021, 1C-Soft LLC and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     1C-Soft LLC - initial API and implementation
 *******************************************************************************/
package com.e1c.ssl.bsl;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.util.Pair;

import com._1c.g5.v8.dt.bsl.model.Expression;
import com._1c.g5.v8.dt.bsl.model.ExtendedCollectionType;
import com._1c.g5.v8.dt.bsl.model.Invocation;
import com._1c.g5.v8.dt.mcore.DerivedProperty;
import com._1c.g5.v8.dt.mcore.Environmental;
import com._1c.g5.v8.dt.mcore.McoreFactory;
import com._1c.g5.v8.dt.mcore.McorePackage;
import com._1c.g5.v8.dt.mcore.Property;
import com._1c.g5.v8.dt.mcore.Type;
import com._1c.g5.v8.dt.mcore.TypeContainerRef;
import com._1c.g5.v8.dt.mcore.TypeItem;
import com._1c.g5.v8.dt.mcore.util.McoreUtil;
import com._1c.g5.v8.dt.platform.IEObjectProvider;
import com._1c.g5.v8.dt.platform.IEObjectTypeNames;
import com._1c.g5.v8.dt.platform.version.Version;
import com.google.common.collect.Lists;

/**
 * Extension computer of invocation types of 1C:SSL API module function {@code Common.FixedData()} that
 * returns the fixed structure.
 *
 * @author Artem Iliukhin
 *
 */
public class CommonFunctionFixedDataTypesComputer
    extends AbstractCommonModuleObjectAttributeValueTypesComputer
{

    @Override
    public List<TypeItem> getTypes(Invocation inv)
    {
        if (inv.getParams().size() != 1)
            return Collections.emptyList();

        if (!isValidModuleNameInvocation(inv))
            return Collections.emptyList();

        Expression expr = inv.getParams().get(0);
        if (expr == null)
            return Collections.emptyList();

        Environmental envs = EcoreUtil2.getContainerOfType(expr, Environmental.class);

        List<TypeItem> types = this.getTypesComputer().computeTypes(expr, envs.environments());
        if (types.isEmpty())
            return Collections.emptyList();

        Collection<Pair<Collection<Property>, TypeItem>> properties =
            this.getDynamicFeatureAccessComputer().getAllProperties(types, envs.eResource());

        if (properties.isEmpty())
            return Collections.emptyList();

        Iterator<Pair<Collection<Property>, TypeItem>> iterator = properties.iterator();
        Pair<Collection<Property>, TypeItem> all = iterator.next();

        if (all == null)
            return Collections.emptyList();

        IEObjectProvider provider = IEObjectProvider.Registry.INSTANCE.get(McorePackage.Literals.TYPE_ITEM,
            versionSupport.getRuntimeVersionOrDefault(inv, Version.LATEST));

        for (TypeItem type : types)
        {
            if (McoreUtil.getTypeName(type).equals(IEObjectTypeNames.STRUCTURE))
            {
                TypeItem item = provider.getProxy(IEObjectTypeNames.FIXED_STRUCTURE);
                return computeStructureTypes(all.getFirst(), item, inv);
            }
            else if (type instanceof ExtendedCollectionType
                && McoreUtil.getTypeName(type).equals(IEObjectTypeNames.ARRAY))
            {
                TypeItem item = provider.getProxy(IEObjectTypeNames.FIXED_ARRAY);
                return computeTypes(type, item, inv);
            }
            else if (McoreUtil.getTypeName(type).equals(IEObjectTypeNames.MAP))
            {
                TypeItem item = provider.getProxy(IEObjectTypeNames.FIXED_MAP);
                return computeTypes(type, item, inv);
            }
        }

        return Collections.emptyList();
    }

    private List<TypeItem> computeTypes(TypeItem sourceType, TypeItem item, EObject context)
    {
        Type type = EcoreUtil2.cloneWithProxies((Type)EcoreUtil.resolve(item, context));

        if (sourceType instanceof Type)
        {
            TypeContainerRef containerRef = McoreFactory.eINSTANCE.createTypeContainerRef();
            containerRef.getTypes().addAll(((Type)sourceType).getCollectionElementTypes().allTypes());
            type.setCollectionElementTypes(containerRef);
            type.setIterable(type.isIterable());
        }

        List<TypeItem> collectionTypes = Lists.newArrayList();
        collectionTypes.add(type);

        return collectionTypes;
    }

    private List<TypeItem> computeStructureTypes(Collection<Property> properties, TypeItem item, EObject context)
    {
        Type type = EcoreUtil2.cloneWithProxies((Type)EcoreUtil.resolve(item, context));

        List<DerivedProperty> derivedProperties =
            properties.stream().filter(prop -> prop instanceof DerivedProperty).map(prop -> {
                DerivedProperty property = (DerivedProperty)EcoreUtil2.cloneWithProxies(prop);
                property.setWritable(false);
                return property;
            }).collect(Collectors.toList());

        type.getContextDef().getProperties().addAll(derivedProperties);

        List<TypeItem> collectionTypes = Lists.newArrayList();
        collectionTypes.add(type);

        return collectionTypes;
    }
}