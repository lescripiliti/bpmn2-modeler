/******************************************************************************* 
 * Copyright (c) 2011, 2012 Red Hat, Inc. 
 *  All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 *
 * @author Ivar Meikas
 ******************************************************************************/
package org.eclipse.bpmn2.modeler.core.features;

import org.eclipse.bpmn2.BaseElement;
import org.eclipse.bpmn2.modeler.core.utils.FeatureSupport;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.IReason;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.impl.AbstractUpdateFeature;
import org.eclipse.graphiti.features.impl.Reason;
import org.eclipse.graphiti.mm.algorithms.AbstractText;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;

/**
 * This is the Graphiti UpdateFeature class for all BPMN2 model elements that
 * subclass {@link BaseElement}.
 */
public abstract class AbstractUpdateBaseElementFeature extends AbstractBpmn2UpdateFeature {

	/**
	 * Instantiates a new UpdateFeature.
	 *
	 * @param fp the Feature Provider
	 */
	public AbstractUpdateBaseElementFeature(IFeatureProvider fp) {
		super(fp);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.graphiti.func.IUpdate#updateNeeded(org.eclipse.graphiti.features.context.IUpdateContext)
	 */
	@Override
	public IReason updateNeeded(final IUpdateContext context) {
		IReason reason = super.updateNeeded(context);
		if (reason.toBoolean())
			return reason;

		PictogramElement pe = context.getPictogramElement();
		if (pe instanceof ContainerShape) {
			String shapeValue = FeatureSupport.getShapeValue(context);
			if (shapeValue==null)
				shapeValue = ""; //$NON-NLS-1$
			String businessValue = FeatureSupport.getBusinessValue(context);
			if (businessValue==null)
				businessValue = ""; //$NON-NLS-1$
	
			boolean updateNeeded = !shapeValue.equals(businessValue);
			
			if (updateNeeded) {
				// try to update immediately
				final boolean result[] = new boolean[1];
				TransactionalEditingDomain domain = getFeatureProvider().getDiagramTypeProvider().getDiagramBehavior().getEditingDomain();
				domain.getCommandStack().execute(new RecordingCommand(domain) {
					@Override
					protected void doExecute() {
						result[0] = update(context);
					}
				});
				if (result[0]==false)
					return Reason.createTrueReason(Messages.AbstractUpdateBaseElementFeature_Name);
			}
		}
		
		return Reason.createFalseReason();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.graphiti.func.IUpdate#update(org.eclipse.graphiti.features.context.IUpdateContext)
	 */
	@Override
	public boolean update(IUpdateContext context) {
		PictogramElement pe = context.getPictogramElement();
		if (pe instanceof ContainerShape) {
			ContainerShape cs = (ContainerShape) pe;
			for (Shape shape : cs.getChildren()) {
				if (shape.getGraphicsAlgorithm() instanceof AbstractText) {
					AbstractText text = (AbstractText) shape.getGraphicsAlgorithm();
					String value = FeatureSupport.getBusinessValue(context);
					if (value == null) {
						value = ""; //$NON-NLS-1$
					}
					text.setValue(value);
					return true;
				}
			}
		}
		return false;
	}

}