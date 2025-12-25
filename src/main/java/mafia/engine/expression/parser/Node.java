package mafia.engine.expression.parser;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import mafia.engine.expression.lexer.Token;
import mafia.engine.expression.lexer.Token.Type;

@Accessors(fluent = true)
public class Node implements Cloneable {
    
    @NonNull @Getter
    private final Type type;

    @NonNull @Getter
    private final String value;

    @Getter @Setter
    private Node left, right;

    public Node(Type t, String s) {
        type = t;
        value = s;
    }

    public Node(Type t, String s, Node left) {
        this(t, s);
        this.left = left;
    }

    public Node(Type t, String s, Node left, Node right) {
        this(t, s);
        this.left = left;
        this.right = right;
    }

    public Node(Token token) {
        this(token.type(), token.value());
    }

    public Node(Token token, Node left) {
        this(token.type(), token.value());
    }

    public Node(Token token, Node left, Node right) {
        this(token.type(), token.value(), left, right);
    }

    public String detailedString() {
        StringBuilder sb = new StringBuilder();
        if (left != null) {
            sb.append("(" + left.detailedString() + " ");
        } else {
            sb.append("(");
        }

        sb.append(new Token(type, value).toString());

        if (right != null) {
            sb.append(" " + right.detailedString() + ")");
        } else {
            sb.append(")");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        str(this, 0, sb);
        return sb.toString();
    }

    public String flattenString() {
        StringBuilder sb = new StringBuilder();
        flattenStr(this, sb);
        return sb.toString();
    }

    private void flattenStr(Node node, StringBuilder sb) {
        if (node == null) {
            sb.append("null");
            return;
        }

        // Leaf node
        if (node.left == null && node.right == null) {
            sb.append(node.value);
            return;
        }

        sb.append("(");

        if (node.left != null) {
            sb.append(node.value).append(" ");
            flattenStr(node.left, sb);
            sb.append(" ");
        } else {
            // sb.append(indent).append("null\n");
        }

        if (node.right != null) {
            sb.append(node.value).append(" ");
            flattenStr(node.right, sb);
            sb.append(" ");
        } else {
            // sb.append(indent).append("null\n");
        }

        sb.append(")");        
    }

    private void str(Node node, int depth, StringBuilder sb) {
        if (node == null)
            return;

        String indent = " ".repeat((depth + 1) * 4);
        
        if (node.value == null && node.type == null)
            sb.append("empty node").append("\n");
        else
            sb.append("type: ").append(node.type).append(" value: ").append(node.value).append("\n");

        if (node.left != null) {
            sb.append(indent);
            str(node.left,  depth + 1, sb);
        } else {
            // sb.append(indent).append("null\n");
        }
        
        if (node.right != null) {
            sb.append(indent);
            str(node.right,  depth + 1, sb);
        } else {
            // sb.append(indent).append("null\n");
        }  
    }

    @Override
    public Node clone() {
        return new Node(
            type,
            value,
            (this.left != null) ? this.left.clone() : null,
            (this.right != null) ? this.right.clone() : null
        );
    }

    public boolean isLeaf() {
        return left == null && right == null;
    }
}
