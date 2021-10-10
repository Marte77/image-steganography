package pt.sapo.dynip.martinho;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class GUI extends JFrame {
    JButton botaoEscolherImagem,botaoInserirString,botaoObterString;

    JTextField textField= new JTextField(20);
    JFileChooser fileChooser = new JFileChooser();
    JPanel panel = new JPanel();
    File fich;
    String mensagem = "", fichName = "", fichExt = "", fichPath = "", fichNameWithoutExt = "", stringDelimiter = "#####";
    public GUI(String titulo){
        super(titulo);
        botaoEscolherImagem = new JButton("Escolher imagem:");
        botaoInserirString= new JButton("Inserir mensagem na imagem");
        botaoObterString= new JButton("Obter mensagem de imagem");
        setBotaoInserirStringActive(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //terminar a app quando se carrega no X
        inicializarGUI();
    }

    private void inicializarGUI(){
        this.setSize(500,500);
        this.setVisible(true);
        panel.add(botaoEscolherImagem, BorderLayout.NORTH);
        panel.add(botaoInserirString, BorderLayout.CENTER);
        panel.add(botaoObterString, BorderLayout.SOUTH);
        panel.add(textField,BorderLayout.NORTH);
        botaoEscolherImagem.addActionListener(actionEvent -> {
            int result = fileChooser.showOpenDialog(botaoEscolherImagem);
            if(result == JFileChooser.APPROVE_OPTION){
                fich = fileChooser.getSelectedFile();
                fichExt = fich.getName().substring(fich.getName().lastIndexOf(".")+1);
                if(!(fichExt.equals("png") || fichExt.equals("jpg") || fichExt.equals("jpeg"))){
                    criarPopup("Tipo de ficheiro nao suportado!",4,null);
                    fichExt = "";
                    return;
                }

                setBotaoInserirStringActive(true);

                fichName = fich.getName();

                fichNameWithoutExt = fichName.substring(0,fichName.lastIndexOf("."));
                fichPath = fich.getPath().substring(0,fich.getPath().indexOf(fichName));

                //fichPath = fich.getPath().replaceFirst(fichName,"");

            }
        });
        textField.getDocument().addDocumentListener(documentListener);

        botaoInserirString.addActionListener(actionEvent ->{
            if(mensagem.isEmpty()){
                criarPopup("Insira uma mensagem!",4, null);
                return;
            }
            try {
                guardarMensagemNaImagem();
            } catch (IOException e) {
                criarPopup(e.getMessage(),4,null);
            }
        });

        botaoObterString.addActionListener(actionEvent ->{
            int result = fileChooser.showOpenDialog(botaoEscolherImagem);
            if(result == JFileChooser.APPROVE_OPTION){
                fich = fileChooser.getSelectedFile();
                fichExt = fich.getName().substring(fich.getName().lastIndexOf(".")+1);
                if(!(fichExt.equals("png") || fichExt.equals("jpg") || fichExt.equals("jpeg"))){
                    criarPopup("Tipo de ficheiro nao suportado!",4,null);
                    fichExt = "";
                    return;
                }

                setBotaoInserirStringActive(true);

                fichName = fich.getName();

                fichNameWithoutExt = fichName.substring(0,fichName.lastIndexOf("."));
                fichPath = fich.getPath().substring(0,fich.getPath().indexOf(fichName));

                try {
                    obterMensagemNaImagem();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        this.add(panel);
    }

    void criarPopup(String mensagem_, int urgencia, String titulo){

        switch (urgencia){
            case 1:
                if(titulo == null)
                    titulo = "mensagem";
                JOptionPane.showMessageDialog(this,mensagem_,titulo,JOptionPane.PLAIN_MESSAGE);
                break;
            case 2:
                JOptionPane.showMessageDialog(this,mensagem_,"Output",JOptionPane.INFORMATION_MESSAGE);
                break;
            case 3:
                JOptionPane.showMessageDialog(this,mensagem_,"Aviso",JOptionPane.WARNING_MESSAGE);
                break;
            case 4:
                JOptionPane.showMessageDialog(this,mensagem_,"Erro",JOptionPane.ERROR_MESSAGE);
                break;
        }
    }

    void copiarImagem(File novoFich) throws IOException {
        FileChannel sourceChannel = null;
        FileChannel destChannel = null;
        try {
            sourceChannel = new FileInputStream(fich).getChannel();
            destChannel = new FileOutputStream(novoFich).getChannel();
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        }finally{
            sourceChannel.close();
            destChannel.close();
        }
    }
    public static String convertStringToBin(String msg){
        StringBuilder builder = new StringBuilder();
        char[] chars = msg.toCharArray();
        for(char cha : chars){
            builder.append(
                    String.format("%8s", Integer.toBinaryString(cha)) // auto cast char to int
                            .replaceAll(" ","0")
            );
        }
        return builder.toString();
    }
    void guardarMensagemNaImagem() throws IOException {
        File ficheiroFinal =new File(fichPath + fichNameWithoutExt + "_edit."+fichExt);
        copiarImagem(ficheiroFinal);
        BufferedImageRGB imagem = new BufferedImageRGB(ficheiroFinal);
        String mensagemEmBin = convertStringToBin(mensagem + stringDelimiter);
        char[] charArray = mensagemEmBin.toCharArray();
        int iteradorMensagemBin = 0, lenMensagemBin = charArray.length;
        if(lenMensagemBin > Math.floor(imagem.width * imagem.height * 3 / 8) || lenMensagemBin > Math.floor(imagem.width * imagem.height * 4 / 8))
            criarPopup("Mensagem é maior que a imagem", 4, null);
        outerloop:
        for(int x = 0; x < imagem.width; x++){
            for(int y = 0; y < imagem.height; y++){
                int[] pixel = imagem.getPixel(x,y);
                if(iteradorMensagemBin < lenMensagemBin){
                    pixel[0] = setBitAtNPosition(pixel[0],0,Character.getNumericValue(charArray[iteradorMensagemBin]));
                    iteradorMensagemBin++;
                }
                if(iteradorMensagemBin < lenMensagemBin){
                    pixel[1] = setBitAtNPosition(pixel[1],0,Character.getNumericValue(charArray[iteradorMensagemBin]));
                    iteradorMensagemBin++;
                }
                if(iteradorMensagemBin < lenMensagemBin){
                    pixel[2] = setBitAtNPosition(pixel[2],0,Character.getNumericValue(charArray[iteradorMensagemBin]));
                    iteradorMensagemBin++;
                }
                if(imagem.hasAlpha && iteradorMensagemBin < lenMensagemBin){
                    pixel[3] = setBitAtNPosition(pixel[3],0,Character.getNumericValue(charArray[iteradorMensagemBin]));
                    iteradorMensagemBin++;
                }
                System.out.println("\t"+Arrays.toString(pixel));
                imagem.imagem.setRGB(x,y,BufferedImageRGB.convertPixelArrayToInt(pixel));
                if(iteradorMensagemBin >= lenMensagemBin){
                    break outerloop;
                }
            }
        }
        ImageIO.write(imagem.imagem,fichExt,ficheiroFinal);
        criarPopup("Concluido", 2, null);
    }

    void obterMensagemNaImagem() throws IOException {

        BufferedImageRGB imagem = new BufferedImageRGB(fich);
        StringBuilder stringBuilder = new StringBuilder();
        outerloop:
        for(int x = 0; x< imagem.width; x++) {
            for (int y = 0; y < imagem.height; y++) {
                int[] pixel = imagem.getPixel(x,y);
                stringBuilder.append(getNLeastSignificantBits(1,pixel[0]));
                stringBuilder.append(getNLeastSignificantBits(1,pixel[1]));
                stringBuilder.append(getNLeastSignificantBits(1,pixel[2]));
                if(imagem.hasAlpha)
                    stringBuilder.append(getNLeastSignificantBits(1,pixel[3]));
            }
        }
        String[] aux = stringBuilder.toString().split("(?<=\\G.{8})");//8bits para cada char
        StringBuilder mensagemObtidaBuilder = new StringBuilder();
        for(String str: aux){
            int i = Integer.parseInt(str,2);
            char c = (char)i;
            mensagemObtidaBuilder.append(c);
        }


        String mensagemObtida = mensagemObtidaBuilder.toString();
        mensagemObtida=mensagemObtida.substring(0,mensagemObtida.indexOf(stringDelimiter));
        criarPopup(mensagemObtida,2,null);
    }

    void setBotaoInserirStringActive(boolean b){
        botaoInserirString.setEnabled(b);
    }

    public static int getNLeastSignificantBits(int N, int number){
        return number & ((1 << N) - 1);
    }
    public static int setBitAtNPosition(int number, int N, int bit){
        int mask = 1 << N;
        return (number & ~mask) | ((bit << N) & mask);
    }
    DocumentListener documentListener = new DocumentListener() {
        void guardar(){
            mensagem = textField.getText();
        }
        @Override
        public void insertUpdate(DocumentEvent e) {
            guardar();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            guardar();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            guardar();
        }
    };
}
