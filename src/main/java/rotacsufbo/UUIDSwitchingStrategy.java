package rotacsufbo;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.*;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class UUIDSwitchingStrategy implements SwitchingStrategy {
    private List<List<String>> uuidList = new ArrayList<>();


    private void createUUID(List<List<Statement>> statements, List<MethodDeclaration> methods) {
        for (MethodDeclaration m: methods) {
            List<String> randomIdList = new ArrayList<>();
            for (Statement s: statements.get(methods.indexOf(m))) {
                randomIdList.add(UUID.randomUUID().toString());
            }
            randomIdList.add(UUID.randomUUID().toString()); //this one goes nowhere
            uuidList.add(randomIdList);
        }

    }

    @Override
    public void createSwitchStatement(ClassOrInterfaceDeclaration cls, List<List<Statement>> statements, List<List<Statement>> declarations, List<MethodDeclaration> methods) {
        createUUID(statements, methods);
        for (MethodDeclaration m : methods) {
            SwitchStmt switchStatement = new SwitchStmt();
            switchStatement.setSelector(JavaParser.parseExpression(SWITCH_SELECTOR));
            NodeList<SwitchEntryStmt> entries = new NodeList<>();
            // This value can be changed based on the strategy of randomization
//            System.out.println(uuidList);
            List<String> idList = uuidList.get(methods.indexOf(m));
            int switchToValueIndex = 0;
            String startSwitch = idList.get(switchToValueIndex);
            String switchToValue = idList.get(switchToValueIndex);
            OpaquePredator op = new OpaquePredator();


            for (Statement stmt : statements.get(methods.indexOf(m))) {
                SwitchEntryStmt entry = new SwitchEntryStmt();
                NodeList<Statement> entryStatements = new NodeList<>();
                entry.setLabel(JavaParser.parseExpression("\"" + switchToValue + "\""));
                switchToValueIndex++;
                switchToValue = idList.get(switchToValueIndex);
                // put the if statement here, wrap up the statements!
                IfStmt ifStatement = new IfStmt();

                // trueFirst or falseFirst?
                boolean trueFirst = ThreadLocalRandom.current().nextBoolean();

                if (trueFirst) {
                    ifStatement.setCondition(JavaParser.parseExpression(op.makeTrueExpression("OPAQUES") +""));
                    Statement sRandom = statements.get(methods.indexOf(m)).get(ThreadLocalRandom.current().nextInt(0, statements.get(methods.indexOf(m)).size()));
                    BlockStmt wrapperA = new BlockStmt();
                    BlockStmt wrapperB = new BlockStmt();
                    wrapperA.addStatement(stmt);
                    wrapperB.addStatement(sRandom);
                    ifStatement.setThenStmt(wrapperA);
                    ifStatement.setElseStmt(wrapperB); //wontrun
                } else {
                    ifStatement.setCondition(JavaParser.parseExpression(op.makeFalseExpression("OPAQUES") +""));
                    Statement sRandom = statements.get(methods.indexOf(m)).get(ThreadLocalRandom.current().nextInt(0, statements.get(methods.indexOf(m)).size()));
                    BlockStmt wrapperA = new BlockStmt();
                    BlockStmt wrapperB = new BlockStmt();

                    wrapperA.addStatement(sRandom);
                    wrapperB.addStatement(stmt);
                    ifStatement.setThenStmt(wrapperA); //wontrun
                    ifStatement.setElseStmt(wrapperB);

                }

//                entryStatements.add(stmt);
                entryStatements.add(ifStatement);
                // Compiler will pick up dead code if there's something after a return;
                if (!stmt.isReturnStmt()) {

                    entryStatements.add(JavaParser.parseStatement(SWITCH_SELECTOR + " = \"" + switchToValue + "\";"));
                    entryStatements.add(JavaParser.parseStatement("break;"));
                }


                entry.setStatements(entryStatements);

                entries.add(entry);
            }

            //TODO: Add random switch statements.
            for (int i = 0; i < 5; i++) {
                SwitchEntryStmt entry = new SwitchEntryStmt();
                NodeList<Statement> entryStatements = new NodeList<>();
                entry.setLabel(JavaParser.parseExpression("\"" + UUID.randomUUID().toString() + "\""));
                // Have some sort of dummy code here.
                Statement sRandom = statements.get(methods.indexOf(m)).get(ThreadLocalRandom.current().nextInt(0, statements.get(methods.indexOf(m)).size()));
                entryStatements.add(sRandom);
                entryStatements.add(JavaParser.parseStatement(SWITCH_SELECTOR + " = \"" + UUID.randomUUID().toString() + "\";"));
                entryStatements.add(JavaParser.parseStatement("break;"));

                entry.setStatements(entryStatements);
                entries.add(entry);

            }

            // Set a default one to break out of the loop
            NodeList<Statement> defaultSwitch = new NodeList<>();
            defaultSwitch.add(JavaParser.parseStatement(WHILE_VARIABLE + " = false;"));
            Collections.shuffle(entries); // so it's not, add this BEFORE the default in case
            entries.add(new SwitchEntryStmt(null, defaultSwitch));
            switchStatement.setEntries(entries);

            WhileStmt whileStatement = new WhileStmt();
            whileStatement.setCondition(JavaParser.parseExpression(WHILE_VARIABLE));
            whileStatement.setBody(switchStatement);

            BlockStmt blockStatement = new BlockStmt();

            for (Statement d : declarations.get(methods.indexOf(m))) {
                blockStatement.addStatement(d);
            }
            blockStatement.addStatement(JavaParser.parseStatement("String " + SWITCH_SELECTOR + " = \"" + startSwitch + "\";"));
            blockStatement.addStatement(JavaParser.parseStatement("boolean " + WHILE_VARIABLE + " = true;"));
            // generate array for opaque thing
            String stm = "int[] OPAQUES = new int[]{" + Arrays.toString(op.getOpaques()).substring(1, Arrays.toString(op.getOpaques()).length()-1) + "};";
            blockStatement.addStatement(JavaParser.parseStatement(stm));
            blockStatement.addStatement(whileStatement);


            // Have to return something otherwise it will be upset!
            if (!m.getTypeAsString().equals("void")) {
                String returnNothing = "return " + DefaultsHelper.getDefault(m.getTypeAsString()) + ";";
                blockStatement.addStatement(JavaParser.parseStatement(returnNothing));
            }

            m.setBody(blockStatement);
//            System.out.println(cls);
        }
    }
}
