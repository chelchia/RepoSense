package git;

import dataObject.Author;
import dataObject.CommitInfo;
import dataObject.RepoConfiguration;
import system.CommandRunner;
import util.Constants;

import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by matanghao1 on 5/6/17.
 */
public class GitLogger {

    private static final Pattern INSERTION_PATTERN = Pattern.compile("([0-9]+) insertions");
    private static final Pattern DELETION_PATTERN = Pattern.compile("([0-9]+) deletions");

    public static List<CommitInfo> getCommits(String repoRoot, RepoConfiguration config){
        String raw = CommandRunner.gitLog(repoRoot);
        ArrayList<CommitInfo> relevantCommits = parseCommitInfo(raw, config.getAuthorList());
        return relevantCommits;
    }

    private static ArrayList<CommitInfo> parseCommitInfo(String rawResult, List<Author> authors){
        ArrayList<CommitInfo> result = new ArrayList<CommitInfo>();
        String[] rawLines= rawResult.split("\n");
        for (int i=0;i<rawLines.length;i++){
            CommitInfo commit = parseRawLine(rawLines[i],rawLines[++i]);
            //if the commit is done by someone not being analyzed, skip it.
            if (!authors.isEmpty() && !authors.contains(commit.getAuthor())){
                continue;
            }
            result.add(commit);
        }
        Collections.reverse(result);
        return result;
    }

    private static CommitInfo parseRawLine(String infoLine, String statLine){
        String[] elements = infoLine.split(Constants.LOG_SPLITTER);
        String hash = elements[0];
        Author author = new Author(elements[1]);
        Date date = null;
        try {
            date = Constants.GIT_ISO_FORMAT.parse(elements[2]);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String message = elements[3];
        int insertion = getInsertion(statLine);
        int deletion = getDeletion(statLine);
        return new CommitInfo(author,hash,date,message,insertion,deletion);
    }

    private static int getInsertion(String raw){
        return getNumberWithPattern(raw, INSERTION_PATTERN);
    }

    private static int getDeletion(String raw){
        return getNumberWithPattern(raw, DELETION_PATTERN);
    }

    private static int getNumberWithPattern(String raw, Pattern p){
        Matcher m = p.matcher(raw);
        if (m.find()){
            return (Integer.parseInt(m.group(1)));
        } else {
            return 0;
        }
    }
}
