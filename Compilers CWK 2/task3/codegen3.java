// Do not modify the code below except for replacing the "..."!  Don't
// add anything (including "public" declarations), don't remove
// anything (including "public" declarations). Don't wrap it in a
// package, don't make it an innner class of some other class.  If
// your IDE suggsts to change anything below, ignore your IDE. You are
// welcome to add new classes! Please put them into separate files.
//184514
import java.util.List;
class Task3 {
    public static Codegen create() throws CodegenException
    {
        CodegenClass codgen = new CodegenClass();
        return codgen;
    }
}

class CodegenClass implements Codegen
{
    String rars_prog = "";
    int increm_if = 0;
	int increm_repeat = 0;
    int increm_while = 0;
    
    @Override
    public String codegen(Program p) throws CodegenException {
        rars_prog = "";
        for(Declaration d: p.decls) {
            codegenDeclaration(d);
        }
        return rars_prog;
    }

    public void codegenDeclaration(Declaration d) throws CodegenException {
        rars_prog += "\n" + ".data";
        int argsSize = (2 + d.numOfArgs)* 4;
        rars_prog += "\n" + ".text";
        rars_prog += "\n" + d.id + "_entry:";
        rars_prog = rars_prog + "\n\t" + "mv s0 sp" + "\n\t" + "sw ra 0(sp)" + "\n\t" + "addi sp sp -4";
        codegenExp(d.body);
        rars_prog = rars_prog + "\n\t" + "lw ra 4(sp)"
                              + "\n\t" + "addi sp sp " + argsSize
                              + "\n\t" + "lw s0 0(sp)"
                              + "\n\t" + "li a7 10"
							  + "\n\t" + "ecall"
							  + "\n\t" + "jr ra";  
                              
    }
	
	
	    public void codegenComp(Comp c) {
        if(c instanceof Equals) 
		{
            rars_prog += "\n\t" + "beq a0 t1 ";
        } else if(c instanceof Less) 
		{
            rars_prog += "\n\t" + "blt t1 a0 ";
        } else if(c instanceof LessEq) 
		{
            rars_prog += "\n\t" + "ble t1 a0 ";
        } else if(c instanceof Greater) 
		{
            rars_prog += "\n\t" + "bgt t1 a0 ";
        } else if(c instanceof GreaterEq) 
		{
            rars_prog += "\n\t" + "bge t1 a0 ";
        }
    }

    public void codegenBinop(Binop b) {
        if(b instanceof Plus) {
            rars_prog += "\n\t" + "add a0 t1 a0";
        } else if(b instanceof Minus) {
            rars_prog += "\n\t" + "sub a0 t1 a0";
        } else if(b instanceof Times) {
            rars_prog += "\n\t" + "mul a0 t1 a0";
        } else if(b instanceof Div) {
            rars_prog = rars_prog + "\n\t" + "div t1, a0" + "\n\t" + "mflo a0";
        }
    }

    private void codegenInvoke(List<Exp> args, String name) throws CodegenException {
        rars_prog = rars_prog + "\n\t" + "sw s0 0(sp)"
                              + "\n\t" + "addi sp sp -4";
        if(args.isEmpty()) {            
            rars_prog = rars_prog + "\n\t" + "jal " + name + "_entry";
        } else {
            for(int i = args.size() - 1; i >= 0; i--) {
                codegenExp(args.get(i));
                rars_prog = rars_prog + "\n\t" + "sw a0 0(sp)"
                                      + "\n\t" + "addi sp sp -4";                
            }
            rars_prog = rars_prog + "\n\t" + "jal " + name + "_entry";
        }
    }
	
    public void codegenExp(Exp e) throws CodegenException {
        if(e instanceof IntLiteral) 
		{
            rars_prog = rars_prog + "\n\t" + "li a0 " + ((IntLiteral) e).n;
        } else if(e instanceof Variable) 
		{
            int offSet = 4*((Variable) e).x;
            rars_prog = rars_prog + "\n\t" + "lw a0 " + offSet + "(s0)";
        } else if(e instanceof If) 
		{
            increm_if++;
            String elsBr = "else_" + increm_if;
            String thnBr = "then_" + increm_if;
            String ifExit = "exit_" + increm_if;
            codegenExp(((If) e).l);
            rars_prog = rars_prog + "\n\t" + "sw a0 0(sp)" + "\n\t" + "addi sp sp -4";
            codegenExp(((If) e).r);
            rars_prog = rars_prog + "\n\t" + "lw t1 4(sp)" + "\n\t" + "addi sp sp 4";
            codegenComp(((If) e).comp);
            rars_prog += thnBr;
            rars_prog = rars_prog + "\n" + elsBr + ":";
            codegenExp(((If) e).elseBody);
            rars_prog = rars_prog + "\n\t" + "b " + ifExit + "\n" + thnBr + ":";
            codegenExp(((If) e).thenBody);
            rars_prog = rars_prog + "\n" + ifExit + ":";
        } else if(e instanceof Binexp) 
		{
            codegenExp(((Binexp) e).l);
            rars_prog = rars_prog + "\n\t" + "sw a0 0(sp)"
                                  + "\n\t" + "addi sp sp -4";
            codegenExp(((Binexp) e).r);
            rars_prog = rars_prog + "\n\t" + "lw t1 4(sp)"
                                  + "\n\t" + "addi sp sp 4";
            codegenBinop(((Binexp) e).binop);
        } else if(e instanceof Invoke) 
		{
            codegenInvoke(((Invoke) e).args, ((Invoke) e).name);
        } else if(e instanceof Skip) 
		{
            rars_prog += "\n\t nop";
        } else if(e instanceof Seq) 
		{
            codegenExp(((Seq) e).l);
            codegenExp(((Seq) e).r);
        } else if(e instanceof While) 
		{
            increm_while++;
            String loop = "loop_" + increm_while;
            String loop_body = "loop_body_" + increm_while;
            String loop_exit = "loop_exit_" + increm_while;
            rars_prog = rars_prog + "\n" + loop + ":";
            rars_prog += "\n\t" + "la t2, " + loop_exit;
            rars_prog += "\n\t" + "sw t2, 0(sp)";
            rars_prog += "\n\t" + "addi sp sp -4";
            rars_prog += "\n\t" + "la t3, " + loop;
            rars_prog += "\n\t" + "sw t3, 0(sp)";
            rars_prog += "\n\t" + "addi sp sp -4";
            codegenExp(((While) e).l);
            rars_prog = rars_prog + "\n\t" + "sw a0 0(sp)"
                                  + "\n\t" + "addi sp sp -4";
            codegenExp(((While) e).r);
            rars_prog = rars_prog + "\n\t" + "lw t1 4(sp)"
                                  + "\n\t" + "addi sp sp 4";
            codegenComp(((While) e).comp);
            rars_prog += loop_body;
            rars_prog += "\n\t" + "j " + loop_exit;
            rars_prog += "\n" + loop_body + ":";
            codegenExp(((While) e).body);
            rars_prog += "\n\t" + "j " + loop;
            rars_prog = rars_prog + "\n" + loop_exit + ":";
            rars_prog = rars_prog + "\n\t" + "lw t2 4(sp)";
            rars_prog = rars_prog + "\n\t" + "addi sp sp 4";
            rars_prog = rars_prog + "\n\t" + "lw t3 4(sp)";
            rars_prog = rars_prog + "\n\t" + "addi sp sp 4";
        } else if(e instanceof RepeatUntil) 
		{
            increm_repeat++;
            String rept = "repeat_" + increm_repeat;
            String rept_ext = "repeat_exit_" + increm_repeat;
            rars_prog = rars_prog + "\n" + rept + ":";
            rars_prog += "\n\t" + "la t2, " + rept_ext;
            rars_prog += "\n\t" + "sw t2, 0(sp)";
            rars_prog += "\n\t" + "addi sp sp -4";
            rars_prog += "\n\t" + "la t3, " + rept;
            rars_prog += "\n\t" + "sw t3, 0(sp)";
            rars_prog += "\n\t" + "addi sp sp -4";
            codegenExp(((While) e).body);
            codegenExp(((While) e).l);
            rars_prog = rars_prog + "\n\t" + "sw a0 0(sp)"
                                  + "\n\t" + "addi sp sp -4";
            codegenExp(((While) e).r);
            rars_prog = rars_prog + "\n\t" + "lw t1 4(sp)"
                                  + "\n\t" + "addi sp sp 4";
            codegenComp(((While) e).comp);
            rars_prog += rept_ext;
            rars_prog += "\n\t" + "j " + rept;
            rars_prog = rars_prog + "\n" + rept_ext + ":";
            rars_prog = rars_prog + "\n\t" + "lw t2 4(sp)";
            rars_prog = rars_prog + "\n\t" + "addi sp sp 4";
            rars_prog = rars_prog + "\n\t" + "lw t3 4(sp)";
            rars_prog = rars_prog + "\n\t" + "addi sp sp 4";
        } else if(e instanceof Assign) 
		{
            int offSet = 4*((Assign) e).x;
            codegenExp(((Assign) e).e);
            rars_prog = rars_prog + "\n\t" + "sw a0 " + offSet + "(s0)";
        } else if(e instanceof Break) 
		{            
            rars_prog = rars_prog + "\n\t" + "jr t2";
        } else if(e instanceof Continue) 
		{
            rars_prog = rars_prog + "\n\t" + "jr t3";
        } else 
		{
            throw new CodegenException("");
        }
    }
    

}
