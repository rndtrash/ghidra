/* ###
 * IP: GHIDRA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ghidra.app.util.pdb.pdbapplicator;

import java.math.BigInteger;

import ghidra.app.util.bin.format.pdb2.pdbreader.PdbException;
import ghidra.app.util.bin.format.pdb2.pdbreader.type.AbstractModifierMsType;
import ghidra.app.util.bin.format.pdb2.pdbreader.type.AbstractMsType;
import ghidra.program.model.data.DataType;
import ghidra.util.exception.CancelledException;

/**
 * Applier for {@link AbstractModifierMsType} types.
 */
//public class ModifierTypeApplier extends AbstractMsTypeApplier {
public class ModifierTypeApplier extends AbstractMsTypeApplier {

	private boolean isDeferred = false;

	private AbstractMsTypeApplier modifiedTypeApplier = null;

	private static AbstractMsType validateType(AbstractMsType type)
			throws IllegalArgumentException {
		if (!(type instanceof AbstractModifierMsType)) {
			throw new IllegalArgumentException(
				"PDB Incorrectly applying " + type.getClass().getSimpleName() + " to " +
					ModifierTypeApplier.class.getSimpleName());
		}
		return type;
	}

	/**
	 * Constructor for modifier type applier.
	 * @param applicator {@link PdbApplicator} for which this class is working.
	 * @param msType {@link AbstractModifierMsType} to processes.
	 * @throws IllegalArgumentException Upon invalid arguments.
	 */
	public ModifierTypeApplier(PdbApplicator applicator, AbstractMsType msType)
			throws IllegalArgumentException {
		super(applicator, validateType(msType));
	}

	//==============================================================================================
	public void setDeferred() {
		isDeferred = true;
	}

	@Override
	public boolean isDeferred() {
		return isDeferred;
	}

	@Override
	void deferredApply() throws PdbException, CancelledException {
		// Do nothing.  Already applied.  Just needs late resolve
	}

	//==============================================================================================
	@Override
	public BigInteger getSize() {
		if (modifiedTypeApplier == null) {
			return BigInteger.ZERO;
		}
		return modifiedTypeApplier.getSize();
	}

	@Override
	public void apply() throws PdbException, CancelledException {
//		dataType = applyModifierMsType((AbstractModifierMsType) msType);
		applyOrDeferForDependencies();
	}

	private void applyOrDeferForDependencies() {
		AbstractModifierMsType type = (AbstractModifierMsType) msType;
		applyModifierMsType(type);
		AbstractMsTypeApplier modifiedApplier =
			applicator.getTypeApplier(type.getModifiedRecordNumber());
		if (modifiedApplier.isDeferred()) {
			applicator.addApplierDependency(this, modifiedApplier);
			setDeferred();
		}
		else {
//			applyModifierMsType(type);
//			defer(false);
		}
	}

	@Override
	public DataType getDataType() {
		return modifiedTypeApplier.getDataType();
	}

	private DataType applyModifierMsType(AbstractModifierMsType type) {
		modifiedTypeApplier = applicator.getTypeApplier(type.getModifiedRecordNumber());

		return modifiedTypeApplier.getDataType();
	}

//	ghDataTypeDB = applicator.resolve(dataType);

//	boolean underlyingIsCycleBreakable() {
//		// TODO: need to deal with InterfaceTypeApplier (will it be incorporated into
//		// CompostieTypeapplier?) Is it in this list of places to break (i.e., can it contain)?
//		return (modifiedTypeApplier != null &&
//			(modifiedTypeApplier instanceof CompositeTypeApplier ||
//				modifiedTypeApplier instanceof EnumTypeApplier));
//	}

	@Override
	DataType getCycleBreakType() {
		// hope to eliminate the null check if/when modifierTypeApplier is created at time of
		// construction
		if (modifiedTypeApplier == null) {
			return null;
		}
		return modifiedTypeApplier.getCycleBreakType();
	}

	public AbstractMsTypeApplier getModifiedTypeApplier() {
		return modifiedTypeApplier;
	}
}
