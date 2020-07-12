package parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class CppParserSemiNEW {

    File file;
    boolean commentBlock = false;
    int countLOC = 0;
    HashMap<String, Integer> methodsLocDecl;

	public CppParserSemiNEW(File file, HashMap<String, Integer> methodsLocDecl) {
		this.file = file;
		this.methodsLocDecl = methodsLocDecl;
	}

	private ArrayList<String> sourceLines = new ArrayList<String>();
	private ArrayList<String> parsedLines = new ArrayList<String>();

	private void getSourceToArray() {
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.contains("#include") || line.contains("using namespace")) {
					sourceLines.add("");
					continue;
				}
				sourceLines.add(line);
			}
		} catch (IOException ex) {
			Logger.getLogger(cParserSemi.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void parse() {
		sourceLines = new ArrayList<String>();
		parsedLines = new ArrayList<String>();

		/////// parsedLines.add("");
		getSourceToArray();
		
		boolean methodDel = false;

		ArrayList<String> methodParam = new ArrayList<>();
		String methodToPrint = "";

		// ***DEBUG

		for (int indx = 0; indx < sourceLines.size(); indx++) {
			// for (int indx = 0; indx < 30; indx++) {

			String line = sourceLines.get(indx);
			if (!line.trim().equals("")) {
				if (!isCommentLine(line.trim()) && !commentBlock) {

                    countLOC++;
					line = line.trim().replace(" +", " ");

					// Check if is line of Method start
					for (String str : methodsLocDecl.keySet()) {
						if (methodsLocDecl.get(str) == indx) {
							System.out.println("\n\n"+"countLOC: "+countLOC+"\n\n\n");
							if (str.trim().contains(" ")) {
								str = str.trim().replace(" +", " ");
								methodToPrint = "Method:" + str.split(" ")[(str.split(" ").length - 1)].trim() + "(";
							} else {
								methodToPrint = "Method:" + str + "(";
							}
							line = line.replace(str, "").trim().substring(1).trim();

							System.out.println("-----METHOD LINE " + indx + ":" + line);
							methodDel = true;
							break;
						}
					}
					// For everything else Use "usage:"
					if (!methodDel) {
						System.out.println(indx + ", lineInterp");
						this.lineInterp(indx); // ***TEST

					} // For Method declaration
					else {
						// Method declaration get parameters and types
						if (line.length() > 1) {
							line = line.trim().replaceAll(" +", " ");
							if (line.charAt(0) != ')') {
								String[] var = line.split(",");
								for (String str : var) {
									String[] var1 = str.trim().split(" ");
									if (var1[0].equals("const")) {
										methodToPrint = methodToPrint + "" + var1[1].replace("*", "").replace("&", "")
												+ ",";
									} else {
										methodToPrint = methodToPrint + "" + var1[0].replace("*", "").replace("&", "")
												+ ",";
									}
									int position = 1;
									if (var1[var1.length - 1].replace("*", "").replace("&", "").equals("{")) {
										position++;
									}
									if (var1[var1.length - position].replace("*", "").replace("&", "").equals(")")) {
										position++;
									}
									methodParam.add(var1[var1.length - position].replace("*", "").replace("&", ""));
								}
							}
						}
						// Method declaration finish
						if (line.contains("{")) {
							methodDel = false;
							if (methodToPrint.contains(",")) {
								// System.out.println(methodToPrint.substring(0, methodToPrint.length() - 1) +
								// ");");
								this.parsedLines.add(methodToPrint.substring(0, methodToPrint.length() - 1) + ");");
								System.out.println("++++METHOD: "
										+ (methodToPrint.substring(0, methodToPrint.length() - 1) + ");"));
							} else {
								// System.out.println(methodToPrint + ");");
								this.parsedLines.add(methodToPrint + ");");
								System.out.println("++++METHOD: " + (methodToPrint + ");"));
							}
							for (String str : methodParam) {
								if (str.contains(")")) {
									str = str.replace(")", "");
								}
								// System.out.println("parameter#" + str + ";");
								this.parsedLines.add("parameter#" + str + ";");
								System.out.println("+++" + ("parameter#" + str + ";"));
							}
							methodToPrint = "";
							methodParam.clear();
						}
					}
				} else if (commentBlock) { // TODO test if -> */ <code>
					stopCommentBlock(line);
				}
			}
		}

		cleanParsedLines();
		utils.Utilities.writeCSV("./" + file.getName() + "_parsed.txt_new", this.parsedLines, false);
		//utils.Utilities.writeCSV("./" + file.getName() + "_parsed.txt", this.parsedLines, false);
	}
	
	public void cleanParsedLines() {
		for (int i = 0; i < this.parsedLines.size(); i++) {
			this.parsedLines.set(i, this.parsedLines.get(i).replaceAll(";;", ";"));
		}
	}

	private void splitLine(String line, int id) {
		String newLine = replaceWithSpaces(line.trim());

		String[] spl = newLine.split(" ");
		for (String tmp : spl) {

			if (tmp == null || tmp.trim().length() == 0) {
				continue;
			}

			if (tmp.equals("true")) {
				continue;
			} else if (tmp.equals("false")) {
				continue;
			} else {
				try { // in case tmp is a number, we dont need that to be written
					double zzz = Double.parseDouble(tmp);
					continue;
				} catch (NumberFormatException exc) {
				}
			}

			if (tmp.contains("\\.")) {
				this.parsedLines.add("Invocation#" + tmp + "#" + (id + 1) + ";");
				String[] spl2 = tmp.split("\\.");
				this.parsedLines.add("Usage#" + spl2[0] + "#" + (id + 1) + ";");
				continue;
			}
			this.parsedLines.add("Usage#" + tmp + "#" + (id + 1) + ";");
		}

	}

	private int findClosingBracket(int id) {
		int idEnd = id;
		int bal = 0;
		boolean started = false;

		// int kkk =0;
		for (int i = id; i < this.sourceLines.size(); i++) {
			if (this.sourceLines.get(i).contains("{")) {
				String[] split = (this.sourceLines.get(i).split("\\{"));

				if (split.length > 1) {
					bal += split.length - 1;
				} else {
					bal++;
				}
			}

			// kkk++;
			// System.out.println("Line ("+kkk+") "+(i+1)+": "+this.sourceLines.get(i));

			if (!started && bal > 0) {
				started = true;
			}

			if (this.sourceLines.get(i).contains("}")) {
				String[] split = (this.sourceLines.get(i).split("\\}"));

				if (split.length > 1) {
					bal -= split.length - 1;
				} else {
					bal--;
				}
			}

			if (started && bal <= 0) {
				idEnd = i;
				break;
			}
		}
		if (id == idEnd) {
			return idEnd;
			// System.out.println("***Error during END_IF calculation with starting Line id:
			// " + (id+1));
			// throw new IllegalArgumentException("Error during END_IF calculation");
		}

		// System.out.println("!!!!!!!!!!!!!!!!!!IF START @: "+(id+1));
		// System.out.println("!!!!!!!!!!!!!!!!!!IF END @: "+(idEnd+1));

		return idEnd;
	}

	private void ifLine(int id) {
		this.parsedLines.add("BEGIN_IF#" + (id + 1) + ";");

		int idEnd = findClosingBracket(id);

		this.parsedLines.add("END_IF#" + (idEnd + 1) + ";");

		String line = this.sourceLines.get(id).replaceFirst("if", "");
		line = this.replaceWithSpaces(line);

		splitLine(line, id);
	}

	private void elseLine(int id) { // TODO san ton allo parser

		this.parsedLines.add("BEGIN_ELSE#" + (id + 1) + ";");

		int idEnd = findClosingBracket(id);

		this.parsedLines.add("END_IF#" + (idEnd + 1) + ";");

		String line = this.sourceLines.get(id).replaceFirst("else", "");
		line = this.sourceLines.get(id).replaceFirst("if", "");
		line = this.replaceWithSpaces(line);

		splitLine(line, id);
	}

	private void forLine(int id) {
		this.parsedLines.add("BEGIN_FOR#" + (id + 1) + ";");

		int idEnd = findClosingBracket(id);

		this.parsedLines.add("END_FOR#" + (idEnd + 1) + ";");

		String line = this.sourceLines.get(id).replaceFirst("for", "");
		line = this.replaceWithSpaces(line);

		splitLine(line, id);
	}

	private void whileLine(int id) {
		this.parsedLines.add("BEGIN_WHILE#" + (id + 1) + ";");

		int idEnd = findClosingBracket(id);

		this.parsedLines.add("END_WHILE#" + (idEnd + 1) + ";");

		String line = this.sourceLines.get(id).replaceFirst("while", "");
		line = this.replaceWithSpaces(line);

		splitLine(line, id);
	}

	private void lineInterp(int id) {
		/*
		 * TODO add Try-Catch add Switch-Case add Do-While correct Else-If correct Else
		 * remove Variable types from Usage (int, double....)
		 */

		String line = sourceLines.get(id).trim();
		// line = this.replaceWithSpaces(line);
		if (line.startsWith("if")) {
			ifLine(id);
		} else if (line.startsWith("else if") || line.startsWith("else")) {
			elseLine(id);
		} else if (line.startsWith("for")) {
			forLine(id);
		} else if (line.startsWith("while")) {
			whileLine(id);
		} else {
			line = this.replaceWithSpaces(line);
			if (id == 11) {
				System.out.println(line);
			}
			splitLine(sourceLines.get(id), id);
		}

	}

	private String replaceWithSpaces(String line) {
		line = line.replaceAll("\\+", " ");
		line = line.replaceAll("-", " ");
		line = line.replaceAll("\\(", " ");
		line = line.replaceAll("\\)", " ");
		line = line.replaceAll("\\*", " ");
		line = line.replaceAll("/", " ");
		line = line.replaceAll("%", " ");
		line = line.replaceAll("=", " ");
		line = line.replaceAll("&", " ");
		line = line.replaceAll("::", " ");
		line = line.replaceAll(":", " ");
		line = line.replaceAll(";", " ");
		line = line.replaceAll("\\[", " ");
		line = line.replaceAll("\\]", " ");
		line = line.replaceAll("\\{", " ");
		line = line.replaceAll("\\}", " ");
		line = line.replaceAll(",", " ");
		line = line.replaceAll(">", " ");
		line = line.replaceAll("<", " ");
		line = line.replaceAll("\"", " ");
		line = line.replaceAll("'", " ");
		line = line.replaceAll("^", " ");
		line = line.replaceAll("!", " ");
		line = line.replaceAll("\\?", " ");
		line = line.replaceAll("\\|\\|", " ");
		line = line.replaceAll("\\|", " ");
		return line;
	}

	private boolean isCommentLine(String word) {
		if (word.length() > 1) {
			if (word.charAt(0) == '#' || word.charAt(1) == '#') {
				if (word.contains("include")) {
					countLOC++;
				}
				return true;
			}
			if (word.contains("printf(")) {
				countLOC++;
				return true;
			}
			if (word.contains("/*")) {// .substring(0, 2).equals("/*") ){
				String lastTwo = word.substring(word.length() - 2);
				if (lastTwo.equals("*/")) {
					return true;
				} else if (word.contains("*//*")) {
					commentBlock = true;
					return true;
				} else if (word.contains("*/")) {
					String[] newLine = word.split("\\*/");
					// this.line = newLine[1];
					return false;
				}
				commentBlock = true;
				return true;
			}
			return (word.substring(0, 2).equals("//"));
		}
		return false;
	}

	private void stopCommentBlock(String line) {
		if (line.contains("*/")) {
			commentBlock = false;
			if (line.contains("*//*")) {
				commentBlock = true;
			}
		}
	}

	public boolean isNumeric(String str) {
		return (// str.chars().allMatch(Character::isDigit) ||
				// Pattern.matches("([0-9]*)\\.([0-9]*)", str) ||
		Pattern.matches("([0-9]*)", str.charAt(0) + ""));
	}
}
