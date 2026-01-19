from matplotlib import pyplot
from numpy import arange, cos, e, exp, meshgrid, pi, sqrt

# Objective function
def objective(x, y):
    return -20.0 * exp(-0.2 * sqrt(0.5 * (x**2 + y**2))) \
           - exp(0.5 * (cos(2 * pi * x) + cos(2 * pi * y))) + e + 20

# Define the input range
r_min, r_max = -5.0, 5.0
# Sample the input range uniformly in increments of 0.1
xaxis = arange(r_min, r_max, 0.1)
yaxis = arange(r_min, r_max, 0.1)
# Create a meshgrid from the axes
x, y = meshgrid(xaxis, yaxis)
# Calculate the results
results = objective(x, y)

# Create a surface plot with a jet colormap
figure = pyplot.figure()
axis = figure.add_subplot(111, projection='3d')
axis.plot_surface(x, y, results, cmap='jet')

# Show the plot
pyplot.show()
