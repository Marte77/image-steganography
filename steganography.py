import tkinter as tk
from PIL import Image
from tkinter.filedialog import askopenfile
import tkinter.messagebox as tkmessage
import sys
import types
import numpy
import cv2
stringDelimiter = "#####"

def convertStringToBinary(string):    
    if(type(string) == str):
        return ''.join([format(ord(i),'08b') for i in string])
    elif(type(string) == bytes or type(string) == numpy.ndarray):
        return [format(i,'08b') for i in string]
    elif(type(string) == int or type(string) == numpy.uint8):
        return format(string,'08b')
    else:
        raise ValueError("Input type not supported")



def convertDecToBin(num):
    return bin(num).replace("0b", "")



def criarPopUp(text:str, type:int):
    if(type == 0):
        tkmessage.showinfo("Info",text)
    if(type == 1):
        tkmessage.showwarning("Aviso",text)
    if(type == 2):
        tkmessage.showerror("Erro",text)


def buttonFileCallback():
    global canButtonFire

    file = askopenfile()
    global filename 
    filename = file.name
    if(not filename):
        print("file select cancelado")
        return
    ext = file.name.split('.')
    ext = ext[ext.__len__()-1]
    if(ext != "png"):
        canButtonFire = False
        criarPopUp("Só suporta imagens png", 2)
        return
    canButtonFire = True


def steganography(imgpath:str, mensagem:str):
    image = cv2.imread(imgpath)
    msg_length = len(mensagem)
    img_size = image.shape[0] * image.shape[1] * 3 // 8
    if(msg_length > img_size):
        raise ValueError('Tamanho da mensagem é superior ao da imagem')
    mensagem += stringDelimiter #saber quando astring acaba
    mensagemEmBytes = convertStringToBinary(mensagem)
    msg_length = len(mensagemEmBytes)
    iterador_msg = 0
    i = 0
    for values in image:
        for pixel in values:
            pixelantigo = pixel
            r,g,b = convertStringToBinary(pixel)#obter os dados rgb em binario
            if(iterador_msg < msg_length):
                pixel[0] = int(r[:-1] + mensagemEmBytes[iterador_msg],2)
                iterador_msg += 1
            if(iterador_msg < msg_length):
                pixel[1] = int(g[:-1] + mensagemEmBytes[iterador_msg],2)
                iterador_msg += 1
            if(iterador_msg < msg_length):
                pixel[2] = int(b[:-1] + mensagemEmBytes[iterador_msg],2)
                iterador_msg += 1
            if(iterador_msg >= msg_length):
                break
    finalpath = filename.split('.')
    finalpath = finalpath[:finalpath.__len__()-1][0] + " -editado-." + finalpath[finalpath.__len__()-1]
    cv2.imwrite(finalpath,image)


def buttonInsertCallback():
    global file
    stringInput = inputText.get()
    if(not canButtonFire):
        criarPopUp("Sem imagem inserida", 2)
        return
    if(stringInput.__len__() == 0):
        criarPopUp("Sem frase inserida", 2)
        return
    window.title("Alterando...")
    print(stringInput, filename)

    try:
        
        for w in window.winfo_children():
            w.configure(state="disabled")
        #img = Image.open(filename)
        #img = img.convert("RGBA")
        
        """
        for x in range(img.width):
            if(iteradorString >=tamanhoStringBytes):
                break
            for y in range(img.height):
                if(iteradorString >=tamanhoStringBytes):
                    break
                pixel = img.getpixel((x,y))#tuple do pixel
                pixelaux = [pixel[0],pixel[1],pixel[2],pixel[3]]#guardar o pixel na variavel ja que os tuples sao read only

                #cada pixel é constituido por 16 bytes, 4 bytes por cada int * 4 valores (RGBA)
                #stringInBytes[iteradorString + i] esta em decimal, converter para binario
                #1-bytefinal = convertStringToBinary(stringInput[iteradorString])
                
                #1-for i in range(4):
                #1-    pixelaux[i] = convertDecToBin(pixelaux[i]) #converter valores decimais para binarios
                #os dois arrays de cima estao em binario mas em string
                #1-pixelfinal = (pixelaux[0],pixelaux[1],pixelaux[2],pixelaux[3])                
                #1-img.putpixel((x,y), pixelfinal)
                
                
                for p in pixelaux:
                    if(iteradorString>=tamanhoStringBytes):
                        break
                    p = p + ord(stringInput[iteradorString])
                    iteradorString = iteradorString + 1
                
                pixelfinal = (pixelaux[0],pixelaux[1],pixelaux[2],pixelaux[3])
                img.putpixel((x,y),pixelfinal)
                iteradorString = iteradorString + 1
        """
        
        steganography(filename, stringInput)

        
        criarPopUp("Concluído!", 0)
    except IOError:
        criarPopUp("Erro a ler imagem: " + IOError.strerror, 2)
        pass
    except ValueError as e:
        criarPopUp("Erro ao inserir a mensagem: " + str(e), 2)
    for w in window.winfo_children():
        w.configure(state="normal")
    window.title("Colocar frases em imagens")
    

def obterStringDaImagem(imgpath:str):
    imagem = cv2.imread(imgpath)

    dadosbinarios = ""
    for values in imagem:
        for pixel in values:
            r,g,b = convertStringToBinary(pixel)
            dadosbinarios += r[-1] 
            dadosbinarios += g[-1] 
            dadosbinarios += b[-1]
    dadosbinarios = [dadosbinarios[i:i+8] for i in range(0,len(dadosbinarios),8)] 
    #a var dadosbinarios agr é um array onde cada index contem um bit dum caracter da string final em binario
    #por isso converter os bits em char
    stringfinal = ""
    for byte in dadosbinarios:
        stringfinal += chr(int(byte,2))
        if stringfinal[-len(stringDelimiter):] == stringDelimiter:
            break
    return stringfinal[:-len(stringDelimiter)]

def buttonDecodeCallback():
    if(not canButtonFire):
        criarPopUp("Sem imagem inserida", 2)
        return
    window.title("Obtendo string...")
    for w in window.winfo_children():
        w.configure(state="disabled")
    stringFinal = ""
    stringFinal = obterStringDaImagem(filename)


    criarPopUp("Concluído!", 0)
    criarPopUp("Frase obtida:\n"+stringFinal, 0)
    
    for w in window.winfo_children():
        w.configure(state="normal")
    window.title("Colocar frases em imagens")




if __name__ == '__main__':
    window = tk.Tk()
    window.title("Colocar frases em imagens")
    window.geometry("400x200")
    timer_id = None
    canButtonFire = False
    filename = ""
    label = tk.Label(text="Inserir strings dentro de imagens")
    inputText =tk.Entry(window)

    chooseFileButton = tk.Button(text="Imagem:", width= 10, height= 3,bg="white", fg="black", command=buttonFileCallback)
    actionInsertButton = tk.Button(text="Inserir", width= 10, height= 3,bg="white", fg="black", command=buttonInsertCallback)
    actionDecodeButton = tk.Button(text="Decode", width= 10, height= 3,bg="white", fg="black", command=buttonDecodeCallback)
    
    label.pack()
    inputText.pack()
    chooseFileButton.pack()
    actionInsertButton.pack()
    actionDecodeButton.pack()
    window.mainloop()