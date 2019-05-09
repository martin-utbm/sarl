namespace tests
{
	public class Cat
	{
		public string name { get; private set; }
		public int bellyCapacity { get; private set; }
		public int bellyContent { get; set; }
		public boolean isGrumpy { get; set; }

		public Cat(string name, int bellyCapacity)
		{
			this.name = name;
			this.bellyCapacity = bellyCapacity;
			this.bellyContent = bellyCapacity / 2;
			this.isGrumpy = false;
		}

		public virtual string meow()
		{
			String @_r;

			if (this.isGrumpy || this.bellyContent < 0.1 * this.bellyCapacity)
			{
				@_r = "MEOW!";
			}
			else
			{
				@_r = "Meow.";
			}

			return "[" + name + "] " + @_r;
		}
	}
}
