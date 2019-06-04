package de.foellix.aql.operator.horndroid;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import de.foellix.aql.Log;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.Flow;
import de.foellix.aql.datastructure.Reference;
import de.foellix.aql.helper.Helper;

public class SourceAndSinkEditor {
	final List<String> susiStrings;

	public SourceAndSinkEditor(File sourcesAndSinksFile) {
		this.susiStrings = new ArrayList<>();

		final Path susiFile = sourcesAndSinksFile.toPath();
		try {
			final List<String> lines = Files.readAllLines(susiFile, Charset.forName("UTF-8"));
			for (final String line : lines) {
				if (!line.startsWith("%") && (line.contains("_SOURCE_") || line.contains("_SINK_"))) {
					final String jimpleStr = Helper.cut(line, "<", ">");

					this.susiStrings.add(jimpleStr);
				}
			}
		} catch (final Exception e) {
			Log.error("Error while parsing SuSi file: " + sourcesAndSinksFile.getAbsolutePath() + " ("
					+ e.getClass().getSimpleName() + ": " + e.getMessage() + ")");
		}
	}

	public void complete(Answer answerHornDroid) {
		for (final Flow flow : answerHornDroid.getFlows().getFlow()) {
			if (!flow.getReference().isEmpty()) {
				final Reference ref = flow.getReference().iterator().next();
				final String strHD = ref.getStatement().getStatementgeneric()
						.replaceFirst("unknown.pkg.and.Class: unknown.return.Type ", "").replaceAll(", ", ",");
				for (final String strSuSi : this.susiStrings) {
					if (strSuSi.replaceAll(", ", ",").contains(strHD)) {
						Log.msg("Changing statement: " + ref.getStatement().getStatementfull(), Log.NORMAL);
						ref.setStatement(Helper.createStatement("<" + strSuSi + ">", false));
						Log.msg(strHD + "\nfound in SuSi file\n" + strSuSi, Log.DEBUG);
						Log.msg("to: " + ref.getStatement().getStatementfull(), Log.NORMAL);
						break;
					}
				}
			}
		}
	}
}