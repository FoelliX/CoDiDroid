package de.foellix.aql.operator.horndroid;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.foellix.aql.Log;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.Attribute;
import de.foellix.aql.datastructure.Attributes;
import de.foellix.aql.datastructure.Flow;
import de.foellix.aql.datastructure.Permission;
import de.foellix.aql.datastructure.Reference;
import de.foellix.aql.datastructure.handler.AnswerHandler;
import de.foellix.aql.helper.EqualsHelper;
import de.foellix.aql.helper.HashHelper;
import de.foellix.aql.helper.Helper;

public class CheckOperator {
	private static final File SOURCES_AND_SINKS_FILE = new File("data/SourcesAndSinks.txt");

	private final File answerToCheckFile;
	private final File answerHornDroidFile;
	private final Answer answerToCheck;
	private final Answer answerHornDroid;
	private final File resultAnswerFile;

	public static void main(String[] args) {
		final String[] files = args[0].split(", ");
		final File answerToCheckFile = new File(files[0]);
		final File answerHornDroidFile = new File(files[1]);
		new CheckOperator(answerToCheckFile, answerHornDroidFile);
	}

	public CheckOperator(File answerToCheckFile, File answerHornDroidFile) {
		this.answerToCheckFile = answerToCheckFile;
		this.answerHornDroidFile = answerHornDroidFile;
		this.answerToCheck = AnswerHandler.parseXML(answerToCheckFile);
		this.answerHornDroid = AnswerHandler.parseXML(answerHornDroidFile);
		this.resultAnswerFile = getResultFile();

		boolean permissionCheck = false;
		if (this.answerHornDroid.getPermissions() != null
				&& !this.answerHornDroid.getPermissions().getPermission().isEmpty()) {
			for (final Permission p : this.answerHornDroid.getPermissions().getPermission()) {
				if (p.getName().equals("HORNDROID_SUCCESSFULLY_EXECUTED")) {
					permissionCheck = true;
				}
			}
		}

		if (permissionCheck && answerToCheckFile.exists() && this.answerToCheck.getFlows() != null
				&& !this.answerToCheck.getFlows().getFlow().isEmpty() && answerHornDroidFile.exists()) {
			check();
		} else {
			markUnchecked();
		}

		AnswerHandler.createXML(this.answerToCheck, this.resultAnswerFile);
		Log.msg(Helper.toString(this.answerToCheck), Log.NORMAL);
	}

	private File getResultFile() {
		final List<File> files = new ArrayList<>();
		files.add(this.answerToCheckFile);
		files.add(this.answerHornDroidFile);
		return new File("result_" + HashHelper.sha256Hash(Helper.answerFilesAsString(files)) + ".xml");
	}

	private void check() {
		final Collection<Flow> removeFlows = new ArrayList<>();

		// Step 1) Complete HornDroid Answer
		Log.msg("Step 1/2) Completing statements according to known Sources and Sinks", Log.NORMAL);
		final SourceAndSinkEditor sase = new SourceAndSinkEditor(SOURCES_AND_SINKS_FILE);
		sase.complete(this.answerHornDroid);

		// Step 2) Check equality
		Log.msg("Step 2/2) Looking for matches", Log.NORMAL);
		for (final Flow flowTC : this.answerToCheck.getFlows().getFlow()) {
			final Reference refTC = Helper.getTo(flowTC.getReference());
			boolean matchFound = false;
			for (final Flow flowHD : this.answerToCheck.getFlows().getFlow()) {
				final Reference refHD = flowHD.getReference().iterator().next();
				if (EqualsHelper.equals(refTC, refHD)) {
					matchFound = true;
					Log.msg("Found match for: " + Helper.toString(refTC), Log.NORMAL);
					attachAttribute(flowTC, true);
					break;
				}
			}
			if (!matchFound) {
				removeFlows.add(flowTC);
			}
		}

		// Step 3) Remove uncheckable flows
		this.answerToCheck.getFlows().getFlow().removeAll(removeFlows);
	}

	private void markUnchecked() {
		if (this.answerToCheck.getFlows() != null && !this.answerToCheck.getFlows().getFlow().isEmpty()) {
			for (final Flow flowTC : this.answerToCheck.getFlows().getFlow()) {
				attachAttribute(flowTC, false);
			}
		}
	}

	private void attachAttribute(Flow flow, boolean checked) {
		final Attribute attr = new Attribute();
		attr.setName("checked");
		attr.setValue(Boolean.toString(checked));
		if (flow.getAttributes() == null) {
			flow.setAttributes(new Attributes());
		}
		flow.getAttributes().getAttribute().add(attr);
	}
}