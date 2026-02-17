#Author:JosiahFout
#Project: Graphic Design
#Start Date: 2/4/25
#Hours Spent: 2
# Create a 2D space and points with TkInter

import tkinter as tk

#For algebra/math


#Creating space/frame for points
space = tk.Tk()

space.title("Points in 2D space")

canvas_width = 500

canvas_height = 500

canvas = tk.Canvas(width=canvas_width, height=canvas_height, bg="white")

canvas.pack()

points = [

    (5, 2), (5,10),
    #(1,10), (1,12), (6,12), (6,15), (10,15), (10,12),
    #(15,12), (15,10), (11,10), (11,2), (9,2), (9,8), (7,8),
    #(7,2), (5,2)

          ]

for x, y in points:
    canvas.create_oval(x, y, 2*x, 2*y, fill="black", outline="black")



space.mainloop()